import { readdir } from 'fs/promises';
import { join, relative, dirname } from 'path';
import { readFileSync, writeFileSync, writeFileSyncSync } from 'fs';
import { load as loadYaml } from 'js-yaml';
import { mkdir, copyFile } from 'fs/promises';

async function readResources(): Promise<Record<string, string>> {
  const resourceDir = join(__dirname, '../src/generated/resources');
  const resources: Record<string, string> = {};

  async function scanDirectory(dir: string) {
    if(dir.startsWith("script")) {
      return;
    }
    const entries = await readdir(dir, { withFileTypes: true });
    
    for (const entry of entries) {
      const fullPath = join(dir, entry.name);
      if (entry.isDirectory()) {
        await scanDirectory(fullPath);
      } else {
        const relativePath = relative(resourceDir, fullPath);
        const normalizedPath = relativePath.replace(/\\/g, '/');
        resources[normalizedPath] = fullPath;
      }
    }
  }

  await scanDirectory(resourceDir);
  return resources;
}

interface RefactorRule {
  match: string[];
  preset: string;
}

interface RefactorConfig {
  markers: RefactorRule[];
  config: Record<string, any>
}


const configPath = join(__dirname, '../resources/refactor_rules.yml');
const config = loadYaml(readFileSync(configPath, 'utf8')) as RefactorConfig;

function categoryFile(resources: Record<string, string>): Record<string, Record<string, string>> {
  
  const result: Record<string, Record<string, string>> = {};
  
  for (const [relativePath, fullPath] of Object.entries(resources)) {
    const normalizedPath = relativePath.replace(/\\/g, '/');
    let matched = false;
    
    // 检查每个规则
    for (const rule of config.markers) {
      for (const pattern of rule.match) {
        // 改进的 glob 到正则表达式转换
        const regexPattern = pattern
          .replace(/\*\*/g, '.*') 
          .replace(/\*/g, '[^/]*')
          .replace(/\//g, '\\/');
        const regex = new RegExp(`^${regexPattern}$`);
        if (regex.test(normalizedPath)) {
          if (!result[rule.preset]) {
            result[rule.preset] = {};
          }
          result[rule.preset][relativePath] = fullPath;
          matched = true;
          break;
        }
      }
      if (matched) break;
    }
    
    if (!matched) {
      if (!result.default) {
        result.default = {};
      }
      result.default[relativePath] = fullPath;
    }
  }
  
  return result;
}

// 添加工具函数来创建目录
async function ensureDir(dir: string) {
  try {
    await mkdir(dir, { recursive: true });
  } catch (error) {
    if ((error as any).code !== 'EEXIST') {
      throw error;
    }
  }
}

// 处理文件路径转换
function processPath(relativePath: string): string {
  if (relativePath.startsWith('assets/kuayue/')) {
    return relativePath.replace('assets/kuayue/', '');
  }
  return relativePath;
}

// 添加复制函数
async function copyToDestination(sourcePath: string, destPath: string) {
  const destDir = dirname(destPath);
  await ensureDir(destDir);
  await copyFile(sourcePath, destPath);
}

// 修改 defaultProcessor
export async function defaultProcessor(files: Record<string, string>) {
  const categorized: Record<string, string> = {};
  const configures = config.config.default;
  
  for(const rule of configures.rules) {
    const regexes = rule['match'].map((t:string)=>new RegExp(t));
    for(const [relativePath, fullPath] of Object.entries(files)) {
      if(categorized[relativePath]) {
        continue;
      }
      if(regexes.some((regex:RegExp)=>regex.test(relativePath))) {
        categorized[relativePath] = rule['category'];
      }
    }
  }

  console.info(`Captured ${Object.keys(categorized).length} files`);

  const unprocessedFiles = Object.keys(files).filter(t=>!categorized[t]);
  console.info(`Unprocessed files: ${unprocessedFiles.length}:`);
  for(const file of unprocessedFiles) {
    console.info(file);
  }

  const categorizeds : Record<string, string[]> = {};
  for(const [relativePath, category] of Object.entries(categorized)) {
    if(!categorizeds[category]) {
      categorizeds[category] = [];
    }
    categorizeds[category].push(relativePath);
  }

  for(const category of Object.keys(categorizeds)) {
    const files = categorizeds[category];
    console.info(`${category}: ${files.length} files:`);
    for(const file of files) {
      console.info(` |- ${file}`);
    }
  }

  const categories = new Set<string>(Object.values(categorized));
  

  // 添加文件复制逻辑
  for (const [relativePath, category] of Object.entries(categorized)) {
    const sourcePath = files[relativePath];
    const processedPath = processPath(relativePath);
    const destPath = join(__dirname, '../resources/generated', category, processedPath);
    await copyToDestination(sourcePath, destPath);
  }

  // 处理未分类的文件
  for (const relativePath of unprocessedFiles) {
    const sourcePath = files[relativePath];
    const processedPath = processPath(relativePath);
    const destPath = join(__dirname, '../resources/generated/default', processedPath);
    await copyToDestination(sourcePath, destPath);
  }

  // 创建 module.yml
  for(const category of categories) {
    const moduleYml = join(__dirname, '../resources/generated', category, 'module.yml');
    await ensureDir(dirname(moduleYml));
    await writeFileSync(moduleYml, "");
  }
}

// 修改 i18nProcessor
export async function i18nProcessor(files: Record<string, string>) {
  const categorized: Record<string, string[]> = {};
  const configures = config.config.i18n;
  
  for (const [relativePath, fullPath] of Object.entries(files)) {
    try {
      // 读取并解析 JSON 文件
      const content = JSON.parse(readFileSync(fullPath, 'utf8'));
      
      // 遍历所有的 key
      for (const key of Object.keys(content)) {
        // 对每个规则进行匹配
        for (const rule of configures.rules) {
          const regexes = rule.match.map((t: string) => new RegExp(t));
          if (regexes.some((regex: RegExp) => regex.test(key))) {
            if (!categorized[rule.category]) {
              categorized[rule.category] = [];
            }
            if (!categorized[rule.category].includes(key)) {
              categorized[rule.category].push(key);
            }
            break;
          }
        }
      }
    } catch (error) {
      console.error(`Error processing file ${relativePath}:`, error);
    }
  }

  // 输出匹配结果
  console.info('I18n key categories:');
  for (const [category, keys] of Object.entries(categorized)) {
    console.info(`${category}: ${keys.length} keys:`);
    for (const key of keys) {
      console.info(` |- ${key}`);
    }
  }

  // 查找未匹配的键
  const processedKeys = new Set(
    Object.values(categorized).flat()
  );

  const unprocessedKeys = new Set<string>();
  for (const [relativePath, fullPath] of Object.entries(files)) {
    try {
      const content = JSON.parse(readFileSync(fullPath, 'utf8'));
      for (const key of Object.keys(content)) {
        if (!processedKeys.has(key)) {
          unprocessedKeys.add(key);
        }
      }
    } catch (error) {
      // 已经在上面报错了，这里忽略
    }
  }

  if (unprocessedKeys.size > 0) {
    console.info(`Unprocessed keys: ${unprocessedKeys.size}:`);
    for (const key of unprocessedKeys) {
      console.info(key);
    }
  }

  // 为每个分类创建语言文件
  for (const [relativePath, fullPath] of Object.entries(files)) {
    const content = JSON.parse(readFileSync(fullPath, 'utf8'));
    const langFileName = relativePath.split('/').pop()!;

    for (const [category, keys] of Object.entries(categorized)) {
      const categoryContent: Record<string, string> = {};
      for (const key of keys) {
        if(key.startsWith("tips") || key.startsWith("auto.gen"))
          return;
        if (content[key]) {
          categoryContent[key] = content[key];
        }
      }

      const destPath = join(__dirname, '../resources/generated', category, 'lang', langFileName);
      await ensureDir(dirname(destPath));
      await writeFileSync(destPath, JSON.stringify(categoryContent, null, 2));
    }

    // 处理未分类的键
    const defaultContent: Record<string, string> = {};
    for (const key of unprocessedKeys) {
      if(key.startsWith("tips") || key.startsWith("auto.gen"))
        return;
      if (content[key]) {
        defaultContent[key] = content[key];
      }
    }
    const defaultDestPath = join(__dirname, '../resources/generated/default/lang', langFileName);
    await ensureDir(dirname(defaultDestPath));
    await writeFileSync(defaultDestPath, JSON.stringify(defaultContent, null, 2));
  }

  const categories = new Set<string>(Object.keys(categorized));

  // 创建 module.yml
  for(const category of categories) {
    const moduleYml = join(__dirname, '../resources/generated', category, 'module.yml');
    await ensureDir(dirname(moduleYml));
    await writeFileSync(moduleYml, "");
  }
}

// 修改 tagProcessor
export async function tagProcessor(files: Record<string, string>) {
  const configures = config.config.tag;
  
  for (const [relativePath, fullPath] of Object.entries(files)) {
    try {
      // 读取并解析 JSON 文件
      const content = JSON.parse(readFileSync(fullPath, 'utf8'));
      
      if (!Array.isArray(content.values)) continue;

      // 为每个文件创建单独的分类结果
      const fileCategories: Record<string, string[]> = {};
      const processedValues = new Set<string>();
      
      // 对文件中的每个值进行分类
      for (const value of content.values) {
        let matched = false;
        for (const rule of configures.rules) {
          const regexes = rule.match.map((t: string) => new RegExp(t));
          if (regexes.some((regex: RegExp) => regex.test(value))) {
            if (!fileCategories[rule.category]) {
              fileCategories[rule.category] = [];
            }
            fileCategories[rule.category].push(value);
            processedValues.add(value);
            matched = true;
            break;
          }
        }
      }

      // 输出当前文件的分类结果
      console.info(`\nFile: ${relativePath}`);
      console.info('Categories:');
      for (const [category, values] of Object.entries(fileCategories)) {
        console.info(`${category}: ${values.length} values:`);
        for (const value of values) {
          console.info(` |- ${value}`);
        }
      }

      // 查找当前文件中未匹配的值
      const unprocessedValues = content.values.filter(value => !processedValues.has(value));

      const otherJson = JSON.parse(JSON.stringify(content));
      delete otherJson['values'];

      if (unprocessedValues.length > 0 || Object.keys(otherJson).length > 0) {
        console.info(`Unprocessed values in ${relativePath}: ${unprocessedValues.length}:`);
        for (const value of unprocessedValues) {
          console.info(` |- ${value}`);
        }
        // 将未匹配的值写入到未分类的文件中
        const defaultDestPath = join(__dirname, '../resources/generated/default', relativePath);
        await ensureDir(dirname(defaultDestPath));
        await writeFileSync(defaultDestPath, JSON.stringify({values: unprocessedValues, ...otherJson}, null, 2));
      }

      // 为每个分类创建 tag 文件
      for (const [category, values] of Object.entries(fileCategories)) {
        const categoryContent = { ...content, values };
        const processedPath = processPath(relativePath);
        const destPath = join(__dirname, '../resources/generated', category, processedPath);
        await ensureDir(dirname(destPath));
        await writeFileSync(destPath, JSON.stringify(categoryContent, null, 2));
      }
      
    } catch (error) {
      console.error(`Error processing file ${relativePath}:`, error);
    }

  }
}

// 添加 animModelProcessor
export async function animModelProcessor(files: Record<string, string>) {
  const configures = config.config.anim_model;
  
  for (const [relativePath, fullPath] of Object.entries(files)) {
    try {
      // 读取并解析 JSON 文件
      const content = JSON.parse(readFileSync(fullPath, 'utf8'));
      
      if (!Array.isArray(content.model)) continue;

      // 为每个文件创建单独的分类结果
      const fileCategories: Record<string, string[]> = {};
      const processedModels = new Set<string>();
      
      // 对文件中的每个模型进行分类
      for (const model of content.model) {
        let matched = false;
        for (const rule of configures.rules) {
          const regexes = rule.match.map((t: string) => new RegExp(t));
          if (regexes.some((regex: RegExp) => regex.test(model))) {
            if (!fileCategories[rule.category]) {
              fileCategories[rule.category] = [];
            }
            fileCategories[rule.category].push(model);
            processedModels.add(model);
            matched = true;
            break;
          }
        }
      }

      // 输出当前文件的分类结果
      console.info(`\nFile: ${relativePath}`);
      console.info('Categories:');
      for (const [category, models] of Object.entries(fileCategories)) {
        console.info(`${category}: ${models.length} models:`);
        for (const model of models) {
          console.info(` |- ${model}`);
        }
      }

      // 查找当前文件中未匹配的模型
      const unprocessedModels = content.model.filter(model => !processedModels.has(model));

      const otherJson = JSON.parse(JSON.stringify(content));
      delete otherJson['model'];

      if (unprocessedModels.length > 0 || Object.keys(otherJson).length > 0) {
        console.info(`Unprocessed models in ${relativePath}: ${unprocessedModels.length}:`);
        for (const model of unprocessedModels) {
          console.info(` |- ${model}`);
        }
        // 将未匹配的模型写入到未分类的文件中
        const defaultDestPath = join(__dirname, '../resources/generated/default', relativePath);
        await ensureDir(dirname(defaultDestPath));
        await writeFileSync(defaultDestPath, JSON.stringify({model: unprocessedModels, ...otherJson}, null, 2));
      }

      // 为每个分类创建文件
      for (const [category, models] of Object.entries(fileCategories)) {
        const categoryContent = { ...content, model: models };
        const processedPath = processPath(relativePath);
        const destPath = join(__dirname, '../resources/generated', category, processedPath);
        await ensureDir(dirname(destPath));
        await writeFileSync(destPath, JSON.stringify(categoryContent, null, 2));
      }
      
    } catch (error) {
      console.error(`Error processing file ${relativePath}:`, error);
    }
  }
}

export const processors: Record<string, (files: Record<string, string>) => void> = {
  'default': defaultProcessor,
  'i18n': i18nProcessor,
  'tag': tagProcessor,
  'anim_model': animModelProcessor,
}

async function main() {
  const resources = await readResources();
  console.info(`Found ${Object.keys(resources).length} resources`);
  const categorized = categoryFile(resources);
  console.info('Categorized resources:');
  for (const [category, files] of Object.entries(categorized)) {
    console.info(`${category}: ${Object.keys(files).length} files`);
  }

  for (const category of Object.keys(categorized)) {
    const files = categorized[category];
    if(!processors[category]) {
      console.error(`No processor for category: ${category}`);
      continue;
    }
    processors[category](files);
  }
}



main();