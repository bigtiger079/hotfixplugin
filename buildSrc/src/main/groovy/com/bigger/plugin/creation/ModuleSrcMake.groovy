package com.bigger.plugin.creation
import javax.inject.Inject

class ModuleSrcMake implements Runnable {
//    final Project project
    final String applicationId
    final String moduleName
    @Inject
    ModuleSrcMake(String applicationId,String moduleName) {
        this.applicationId = applicationId
        this.moduleName = moduleName
    }
    @Override
    void run() {
        File moduleDir = new File(moduleName)
        if (moduleDir.exists() && moduleDir.isDirectory()) {
            File srcDir = new File(moduleDir, "src/main/java/${applicationId.replaceAll("\\.", "/")}")
            srcDir.mkdirs()
        }
        File settings = new File("settings.gradle")
        if (settings.exists()) {
            boolean isModuleInclude = false
            settings.eachLine{
                if (it.contains(":${moduleName}")) {
                    isModuleInclude = true
                }
            }
            if (!isModuleInclude) {
                StringWriter writer = new StringWriter()
                writer.write("\rinclude ':${moduleName}'")
                settings.append(writer, "utf-8")
            }
        } else {
            println "settings.gradle not find"
        }
    }
}