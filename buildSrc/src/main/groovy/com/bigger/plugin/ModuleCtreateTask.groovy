import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.OutputDirectory
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
                configs.forEach {
                    def name = it.moduleName
                    def moduleDir = new File(name)
                    if (!moduleDir.exists() | !moduleDir.isDirectory()) {
                        def binding = [applicationId:it.applicationId, packageName:it.packageName, apkName:"${it.moduleName}.apk"]
                        project.copy {
                            expand binding
                            with modulesConfig
                            into name
                        }
                        workerExecutor.submit(TemplateCopy.class) { WorkerConfiguration config ->
                            config.isolationMode = IsolationMode.NONE
                            config.params project, it.applicationId, it.packageName, it.moduleName
                        }
                    }
                }
            }
        }
//        workerExecutor.await()
    }
}