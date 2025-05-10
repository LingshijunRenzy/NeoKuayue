package willow.train.kuayue.builder

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class ModuleCompilePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.register("resourceCompileTask", ResourceCompileTask)
    }

    static class ResourceCompileTask extends DefaultTask {
        @TaskAction
        void compile() {
            File baseDirectory = new File(project.projectDir, "src/generated/resources/")
            File assetsDirectory = new File(baseDirectory, "assets")
            File dataDirectory = new File(baseDirectory, "data")
            try{
                if(assetsDirectory.exists()) {
                    println "Delete old assets directory: ${assetsDirectory.path}"
                    assetsDirectory.deleteDir()
                }
                if(dataDirectory.exists()) {
                    println "Delete old data directory: ${dataDirectory.path}"
                    dataDirectory.deleteDir()
                }
            }catch (Exception ignored){}
            File resourcesDir = new File(project.projectDir, "resources/generated")
            if (resourcesDir.exists() && resourcesDir.isDirectory()) {
                findModuleFiles(resourcesDir)
            } else {
                println "No resources directory found or it is not a directory."
            }
        }

        void findModuleFiles(File dir) {
            File baseDirectory = new File(project.projectDir, "src/generated/resources/")
            HashSet<File> moduleIndexes = new HashSet<>();
            dir.eachFileRecurse { file ->
                if (file.name.endsWith("module.yml")) {
                    // println "Found module file: ${file.path}"
                    moduleIndexes.add(file.parentFile);
                }
            }

            if (moduleIndexes.isEmpty()) {
                println "No module files found."
            } else {
                HashMap<File, ArrayList<File>> conflictMap = new HashMap<>();
                moduleIndexes.stream().map {
                    return loadModuleFiles(it, moduleIndexes, baseDirectory)
                }.forEach {
                    // Add into conflict map
                    it.each { k, v ->
                        if (conflictMap.containsKey(v)) {
                            conflictMap.get(v).add(k);
                        } else {
                            ArrayList<File> list = new ArrayList<>();
                            list.add(k);
                            conflictMap.put(v, list);
                        }
                    }
                }

                println "Found ${conflictMap.size()} files to copy.\n";

                conflictMap.entrySet().parallelStream().forEach {
                    File targetFile = it.getKey();
                    ArrayList<File> sourceFiles = it.getValue();

                    if (sourceFiles.size() > 1) {
                        println "Conflict found for file: ${targetFile.path}"

                        sourceFiles.forEach { file ->
                            println " - Source file: ${file.path}"
                        }

                        // Handle JSON conflict
                        if(targetFile.name.endsWith(".json")) {
                            // Handle JSON conflict resolution, merging all elements
                            JsonElement mergedJson = null

                            sourceFiles.each { sourceFile ->
                                JsonElement sourceJson = JsonParser.parseString(sourceFile.text)
                                if (mergedJson == null) {
                                    mergedJson = sourceJson
                                } else {
                                    mergedJson = deepMergeJson(mergedJson, sourceJson)
                                }
                            }

                            // Write merged JSON to target file
                            if (!targetFile.parentFile.exists()) {
                                targetFile.parentFile.mkdirs()
                            }
                            targetFile.text = new GsonBuilder().setPrettyPrinting().create().toJson(mergedJson)
                        } else {
                            throw new RuntimeException("Unaccpetable conflict found for file: ${targetFile.path}, please resolve it manually.")
                        }
                    } else {
                        // No conflict, copy the file
                        File sourceFile = sourceFiles.get(0);
                        if (!targetFile.parentFile.exists()) {
                            targetFile.parentFile.mkdirs();
                        }
                        sourceFile.withInputStream { input ->
                            targetFile.withOutputStream { output ->
                                output << input
                            }
                        }
                    }
                }
            }
        }

        static HashMap<File, File> loadModuleFiles(File moduleDir, HashSet<File> moduleIndexes, File generatedBase) {
            HashMap<File, File> moduleInfo = new HashMap<>();
            moduleDir.eachFileRecurse {
                if (!it.isFile()) {
                    return;
                }

                if(it.name == "module.yml") {
                    return;
                }

                if (moduleIndexes.parallelStream().anyMatch {s->it.path.startsWith(s.path) && s != moduleDir }) {
                    return;
                }

                String relativePath = moduleDir.relativePath(it);

                File target;

                if(relativePath.startsWith("assets") || relativePath.startsWith("data")) {
                    target = new File(generatedBase, relativePath);
                } else {
                    target = new File(generatedBase, "assets/kuayue/$relativePath");
                }

                moduleInfo.put(it, target);
            }
            return moduleInfo;
        }

        protected static JsonElement deepMergeJson(JsonElement dest, JsonElement src) {
            if (src.isJsonObject()) {
                JsonObject srcObj = src.getAsJsonObject()
                JsonObject destObj = dest.isJsonObject() ? dest.getAsJsonObject() : new JsonObject()

                srcObj.entrySet().each { entry ->
                    String key = entry.getKey()
                    JsonElement value = entry.getValue()

                    if (destObj.has(key)) {
                        destObj.add(key, deepMergeJson(destObj.get(key), value))
                    } else {
                        destObj.add(key, value)
                    }
                }
                return destObj
            } else if (src.isJsonArray()) {
                JsonArray srcArray = src.getAsJsonArray()
                JsonArray destArray = dest.isJsonArray() ? dest.getAsJsonArray() : new JsonArray()

                // Merge arrays by adding all elements from source
                srcArray.each { element ->
                    destArray.add(element)
                }
                return destArray
            } else {
                // For primitive values, return the source value
                return src
            }
        }
    }
}