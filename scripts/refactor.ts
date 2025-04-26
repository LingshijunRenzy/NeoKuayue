import { readdir } from 'fs/promises';
import { join, relative } from 'path';
import { readFileSync } from 'fs';
import { load as loadYaml } from 'js-yaml';

async function readResources(): Promise<Record<string, string>> {
  const resourceDir = join(__dirname, '../src/generated/resources');
  const resources: Record<string, string> = {};

  async function scanDirectory(dir: string) {
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

export function defaultProcessor(files: Record<string, string>) {
  const categorized : Record<string, string> = {};
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
}

export function i18nProcessor(files: Record<string, string>) {
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
}

export const processors: Record<string, (files: Record<string, string>) => void> = {
  'default': defaultProcessor,
  'i18n': i18nProcessor,
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