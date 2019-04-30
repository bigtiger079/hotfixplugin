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
        File configTemp
        def subxposed = project.extensions.create("subxposed", XposedSubPluginExtension.class)

        project.task(type: Exec, 'uploadSubXposedModule') {
            mustRunAfter = ["assembleDebug"]
            group = 'xposed'
            description = 'auto push xposed hot module to android mobile by adb'
            ignoreExitValue = true
            standardOutput = new ByteArrayOutputStream()

            ext.output = {
                return standardOutput.toString()
            }

            doFirst {
                println "on Start Task -> uploadSubXposedModule"
                def debug = project.android.applicationVariants.find{ variants -> variants.name == "debug" }
                def  debugTask = debug.packageApplicationProvider.get()
                File apkFile = new File(debugTask.outputDirectory, debugTask.apkNames[0])
                println "apk -> ${apkFile.path}"
                commandLine adb.path, "push", apkFile.path, "${subxposed.destDir}/${subxposed.apkName}"
                println "on execute cmd: ${commandLine.join(' ')}"
            }
            doLast {
                if(!execResult.exitValue) {//success
                    println "push ${commandLine[2]} success -> ${output()}"
                }
            }
        }

        project.task(type: Exec, 'syncModulesConfigFromRemote') {
            group = 'xposed'
            description = 'auto pull module.json file from mobile and check'
            mustRunAfter = ['uploadSubXposedModule']
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()
            doFirst {
                commandLine adb.path, "pull", "${subxposed.destDir}/modules.json", "${temporaryDir.path}/modules.json"
                println "on execute cmd: ${commandLine.join(' ')}"
            }

            doLast {
                configTemp= new File("${temporaryDir.path}/modules.json")
                if (configTemp.exists()) {
                    def configs = new JsonSlurper().parse(configTemp)
                    println "configs -> ${configs.size()}"
                    def config = configs.find { config ->
                        config.packageName == subxposed.packageName
                    }
                    println "config -> ${config}"
                    println "subxposed -> ${subxposed}"
                    if(config != null) {
                        if ((config.apkName != subxposed.apkName || config.entryClass != subxposed.entryClass)) {
                            config.apkName = subxposed.apkName
                            config.entryClass = subxposed.entryClass
                            def jsonStr = JsonOutput.toJson(configs)
                            println "configs -> ${configs.size()}"
                            jsonFile.withWriter("utf-8") { writer ->
                                writer.write(jsonStr)
                            }
                        }
                    } else {
                        def configInfo = [packageName: subxposed.packageName, apkName:subxposed.apkName, entryClass: subxposed.entryClass]
                        configs.add(configInfo)
                        def jsonStr = JsonOutput.toJson(configs)
                        configTemp.withWriter("utf-8") { writer ->
                            writer.write(jsonStr)
                        }
                    }

                } else {
                    def configInfo = [packageName: subxposed.packageName, apkName:subxposed.apkName, entryClass: subxposed.entryClass]
                    def jsonStr = JsonOutput.toJson([configInfo])
                    configTemp.withWriter("utf-8") { writer ->
                        writer.write(jsonStr)
                    }
//                    configPushTask.execute()
                }
            }
        }

        def unloadModuleTask = project.task(type: Exec, 'unloadModuleOfRemote') {
            group = 'xposed'
            description = 'auto pull module.json file from mobile and remove this module'
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()
            doFirst {
                commandLine adb.path, "pull", "${subxposed.destDir}/modules.json", "${temporaryDir.path}/modules.json"
                println "on execute cmd: ${commandLine.join(' ')}"
            }

            doLast {
                configTemp= new File("${temporaryDir.path}/modules.json")
                if (configTemp.exists()) {
                    def configs = new JsonSlurper().parse(configTemp)
                    println "configs -> ${configs.size()} ${configs}"
                    def config = configs.find { config ->
                        config.packageName == subxposed.packageName && config.apkName == subxposed.apkName && config.entryClass == subxposed.entryClass
                    }
                    println "config -> ${config}"
                    if(config != null) {
                        configs.remove(config)
                        println "configs -> ${configs.size()}"
                        def jsonStr = JsonOutput.toJson(configs)
                        println "jsonStr -> ${jsonStr}"
                        configTemp.withWriter("utf-8") { writer ->
                            writer.write(jsonStr)
                        }
//                        configPushTask.execute()
                    }
                }
            }
        }

        project.task(type: Exec, "updateRemoteModulesConfig") {
            group = 'xposed'
            description = 'push module.json file to mobile'
            ignoreExitValue true
            mustRunAfter = ['syncModulesConfigFromRemote', 'unloadModuleOfRemote']
            standardOutput = new ByteArrayOutputStream()
            doFirst {
                commandLine adb.path, "push", "${configTemp.path}", "${subxposed.destDir}/modules.json"
                println "on execute cmd: ${commandLine.join(' ')}"
            }

            doLast {
                if(!execResult.exitValue) {//success
                    println "push ${commandLine[2]} success -> ${standardOutput.toString()}"
                } else {
                    println "push ${commandLine[2]} failed -> ${standardOutput.toString()}"
                }
            }
        }

//        pluginUploadTask.dependsOn project.tasks.named("build")
//        syncModulesConfigFromRemoteTask.dependsOn pluginUploadTask
//        configPushTask.dependsOn syncModulesConfigFromRemoteTask
//        configPushTask.dependsOn unloadModuleTask
//
//
//        pluginUploadTask.mustRunAfter project.tasks.named("build")
//        syncModulesConfigFromRemoteTask.mustRunAfter pluginUploadTask
//        configPushTask.mustRunAfter syncModulesConfigFromRemoteTask, unloadModuleTask

        project.gradle.taskGraph.afterTask { Task task ->
            if(task.name == "assembleDebug") {
                println "After Task: ${task.name}"
                println project.tasks.named('uploadSubXposedModule').getOrNull().mustRunAfter.getDependencies(project.tasks.named('uploadSubXposedModule').getOrNull())

            }
        }

        project.afterEvaluate {
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
        }
    }
}

class XposedSubPluginExtension {
    String destDir
    String packageName
    String apkName
    String entryClass
}
