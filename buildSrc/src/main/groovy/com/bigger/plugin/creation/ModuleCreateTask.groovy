package com.bigger.plugin.creation

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor
import org.gradle.workers.IsolationMode

import javax.inject.Inject

class ModuleCreateTask extends DefaultTask {
    final WorkerExecutor workerExecutor
    CopySpec modulesConfig = project.copySpec{
        includeEmptyDirs = true
        from 'template'
        exclude "build", "*.class", "*.iml", "src/main/java/*"
    }
    @Inject
    ModuleCreateTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @TaskAction
    void reverseFiles() {
        println "reverseFiles"
        println project
        def configTemp= new File("modules.json")
        if (configTemp.exists()) {
            def configs = new JsonSlurper().parse(configTemp)
            if (configs.size() > 0) {
                configs.forEach { config ->
                    def moduleDir = new File(config.moduleName)
                    if (!moduleDir.exists() | !moduleDir.isDirectory()) {
                        def binding = [
                                applicationId: config.applicationId,
                                packageName: config.packageName,
                                entryClass: "${config.applicationId}.ModuleEntry",
                                apkName: "${config.moduleName}.apk"
                        ]
                        project.copy {
                            expand binding
                            with modulesConfig
                            into config.moduleName
                        }
                        project.copy {
                            expand binding
                            from 'template/src/main/java/com/bigger/template/ModuleEntry.java'
                            into "${config.moduleName}/src/main/java/${config.applicationId.replaceAll('\\.', '/')}/"
                        }
                        workerExecutor.submit(ModuleSrcMake.class) { WorkerConfiguration workerConfiguration ->
                            workerConfiguration.isolationMode = IsolationMode.NONE
                            workerConfiguration.params config.applicationId, config.moduleName
                        }
                    }
                }
            }
        }
//        workerExecutor.await()
    }
}