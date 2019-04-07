package com.bigger.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec


class XposedSubPlugin implements Plugin<Project> {

    void apply(Project project) {

        File adb = project.android.adbExecutable

        def subxposed = project.extensions.create("subxposed", XposedSubPluginExtension.class)

        def pluginUploadTask = project.task(type: Exec, 'uploadSubXposedModule') {
            group = 'xposed'
            description = 'auto push xposed hot module to android mobile by adb'
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()

            ext.output = {
                return standardOutput.toString()
            }

            doFirst {
                def debug = project.android.applicationVariants.find{ variants -> variants.name == "debug" }
                File apkFile = debug.outputs[0].outputFile
                println "apk -> ${apkFile.path}"
                commandLine adb.path, "push", apkFile.path, "${subxposed.destDir}/${subxposed.apkName}"
            }
            doLast {
                println "result -> ${execResult.exitValue}"
                if(!execResult.exitValue) {//success
                    println "output -> ${output()}"
                }
            }
        }


        def configPushTask = project.task(type: Exec, "updateModuleJson") {
            group = 'xposed'
            description = 'push module.json file to mobile'
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()
            doFirst {
                commandLine adb.path, "push", "${temporaryDir.path}/modules.json", "${subxposed.destDir}/modules.json"
            }
        }

        def configCheckTask = project.task(type: Exec, 'checkModuleJson') {
            group = 'xposed'
            description = 'auto pull module.json file from mobile and check'
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()
            doFirst {
                commandLine adb.path, "pull", "${subxposed.destDir}/modules.json", "${temporaryDir.path}/modules.json"
            }

            doLast {
                def jsonFile = new File("${temporaryDir.path}/modules.json")
                if (jsonFile.exists()) {
                    def configs = new JsonSlurper().parse(jsonFile)
                    def config = configs.find { config ->
                        config.packageName == subxposed.packageName
                    }
                    config.apkName = subxposed.apkName
                    config.entryClass = subxposed.entryClass
                    if(config != null && (config.apkName != subxposed.apkName || config.entryClass != subxposed.entryClass)) {
                        config.apkName = subxposed.apkName
                        config.entryClass != subxposed.entryClass
                        def jsonStr = JsonOutput.toJson(configs)
                        jsonFile.withWriter("utf-8") { writer ->
                            writer.write(jsonStr)
                        }
                        configPushTask.execute()
                    }
                } else {
                    def configInfo = [packageName: subxposed.packageName, apkName:subxposed.apkName, entryClass: subxposed.entryClass]
                    def jsonStr = JsonOutput.toJson([configInfo])
                    jsonFile.withWriter("utf-8") { writer ->
                        writer.write(jsonStr)
                    }
                    configPushTask.execute()
                }
            }
        }

        project.gradle.taskGraph.afterTask { Task task ->
            if(task.name == "assembleDebug") {
                if(!subxposed.packageName) {
                    throw new IllegalStateException("请配置subxposed 的 packageName")
                }
                if(!subxposed.entryClass) {
                    throw new IllegalStateException("请配置subxposed 的 entryClass")
                }
                if(!subxposed.destDir) {
                    subxposed.destDir = '/data/local/tmp/hook/'
                }
                if(!subxposed.apkName) {
                    subxposed.apkName = "${project.name}.apk"
                }
                println "apkName -> ${subxposed.apkName}"
                pluginUploadTask.execute()
                configCheckTask.execute()
            }
        }
    }
}

class XposedSubPluginExtension {
    String destDir
    String packageName
    String apkName
    String entryClass
}
