package com.bigger.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class XposedSubPlugin implements Plugin<Project> {

    void apply(Project project) {
        File adb = project.android.adbExecutable

        def xposed = project.extensions.create("xposed", XposedModuleExtension.class)

        project.tasks.register("addModuleToRemote") {
            group = 'xposed'
            description = 'add this module(apk) to mobile'
            mustRunAfter 'packageDebug'
            dependsOn 'uploadSubXposedModule', "updateRemoteModulesConfig"
            doFirst {
                println "start Task -> addModuleToRemote"
            }
            onlyIf {
                !xposed.disabled
            }
        }

        project.tasks.register('uploadSubXposedModule',  Exec) {
            group = 'xposed'
            description = 'auto push xposed hot module to android mobile by adb'
            ignoreExitValue = true
            standardOutput = new ByteArrayOutputStream()
            ext.output = {
                return standardOutput.toString()
            }
            ext.isApkExist = false

            doFirst {
                println "start Task -> uploadSubXposedModule"
                def debug = project.android.applicationVariants.find{ variants -> variants.name == "debug" }
                def  debugTask = debug.packageApplicationProvider.get()
                File apkFile = new File(debugTask.outputDirectory, debugTask.apkNames[0])
                println "apk -> ${apkFile.path}"
                isApkExist = apkFile.exists()
                commandLine adb.path, "push", apkFile.path, "${xposed.destDir}/${xposed.apkName}"
                println "on execute cmd: ${commandLine.join(' ')}"
            }
            doLast {
                if(isApkExist && !execResult.exitValue) {//success
                    println "push ${commandLine[2]} success -> ${output()}"
                }
            }
            onlyIf {
                !xposed.disabled
            }
        }

        project.tasks.register('syncModulesConfigFromRemote',Exec) {
            group = 'xposed'
            description = 'auto pull module.json file from mobile and check'
            ignoreExitValue true
            standardOutput = new ByteArrayOutputStream()
            mustRunAfter "uploadSubXposedModule"
            doFirst {
                commandLine adb.path, "pull", "${xposed.destDir}/modules.json", "${temporaryDir.path}/modules.json"
                println "on execute cmd: ${commandLine.join(' ')}"
            }
            onlyIf {
                !xposed.disabled
            }
        }

        project.tasks.register('addModuleInfoToConfig') {
            group = 'xposed'
            description = 'add info of this module to config file'
            dependsOn 'syncModulesConfigFromRemote'
            doFirst {
                def syncTask = project.tasks.named('syncModulesConfigFromRemote').get()
                def configFile= new File("${syncTask.temporaryDir.path}/modules.json")
                def configs = null
                def write = {
                    if (configs != null) {
                        def jsonStr = JsonOutput.toJson(configs)
                        configFile.withWriter("utf-8") { writer ->
                            writer.write(jsonStr)
                        }
                    }
                }
                if (configFile.exists()) {
                    configs = new JsonSlurper().parse(configFile)
                    println "configs -> ${configs}"
                    def config = configs.find { config ->
                        config.packageName == xposed.packageName
                    }
                    println "config -> ${config}"
                    if(config != null) {
                        if ((config.apkName != xposed.apkName || config.entryClass != xposed.entryClass)) {
                            config.apkName = xposed.apkName
                            config.entryClass = xposed.entryClass
                            write.call()
                        }
                    } else {
                        def configInfo = [packageName: xposed.packageName, apkName:xposed.apkName, entryClass: xposed.entryClass]
                        configs.add(configInfo)
                        write.call()
                    }
                } else {
                    configFile.createNewFile()
                    def configInfo = [packageName: xposed.packageName, apkName:xposed.apkName, entryClass: xposed.entryClass]
                    configs = [configInfo]
                    write.call()
                }
            }
            onlyIf {
                !xposed.disabled
            }
        }

        project.tasks.register('removeModuleInfoFromConfig') {
            group = 'xposed'
            description = 'remove the info of this module from config file'
            dependsOn 'syncModulesConfigFromRemote'

            doFirst {
                def syncTask = project.tasks.named('syncModulesConfigFromRemote').get()
                def configFile= new File("${syncTask.temporaryDir.path}/modules.json")
                if (configFile.exists()) {
                    def configs = new JsonSlurper().parse(configFile)
                    println "configs -> ${configs}"
                    def config = configs.find { config ->
                        config.packageName == xposed.packageName && config.apkName == xposed.apkName && config.entryClass == xposed.entryClass
                    }
                    if(config != null) {
                        configs.remove(config)
                        def jsonStr = JsonOutput.toJson(configs)
                        println "jsonStr -> ${jsonStr}"
                        configFile.withWriter("utf-8") { writer ->
                            writer.write(jsonStr)
                        }
                    }
                }
            }
            onlyIf {
                !xposed.disabled
            }
        }

        project.tasks.register('unloadModuleOfRemote') {
            group  'xposed'
            description  'auto pull module.json file from mobile and remove this module'
            dependsOn 'removeModuleInfoFromConfig', 'updateRemoteModulesConfig'
        }

        project.tasks.register("updateRemoteModulesConfig", Exec) {
            group = 'xposed'
            description = 'push module.json file to mobile'
            ignoreExitValue true
            mustRunAfter 'addModuleInfoToConfig', 'removeModuleInfoFromConfig'
            standardOutput = new ByteArrayOutputStream()
            ext
            doFirst {
                def syncTask = project.tasks.named('syncModulesConfigFromRemote').get()
                def configFile= new File("${syncTask.temporaryDir.path}/modules.json")
                if (configFile.exists()){
                    commandLine adb.path, "push", "${configFile.path}", "${xposed.destDir}/modules.json"
                } else {
                    commandLine 'echo', "${configFile.path} is not exist"
                }
            }

            doLast {
                if(!execResult.exitValue) {//success
                    println "push ${commandLine[2]} success -> ${standardOutput.toString()}"
                } else {
                    println "push ${commandLine[2]} failed -> ${standardOutput.toString()}"
                }
            }
            onlyIf {
                !xposed.disabled
            }
        }
        // BUG: :processDebugAndroidTestManifest -> ERROR: No value has been specified for property 'manifestOutputDirectory'.
//        project.getTasks().whenTaskAdded{ Task task ->
//            if (task.name == "assembleDebug") {
//                task.dependsOn "addModuleToRemote", "addModuleInfoToConfig", "updateRemoteModulesConfig"
//            }
//        }
        project.afterEvaluate {
            if (!xposed.disabled) {
                if(!xposed.packageName) {
                    throw new IllegalStateException("请配置subxposed 的 packageName")
                }
//                if(!xposed.entryClass) {
//                    throw new IllegalStateException("请配置subxposed 的 entryClass")
//                }
                if(!xposed.destDir) {
                    xposed.destDir = '/data/local/tmp/hook/'
                }
                if(!xposed.apkName) {
                    xposed.apkName = "${project.name}.apk"
                }
            }
        }
    }
}

class XposedModuleExtension {
    String destDir
    String packageName
    String apkName
    String entryClass
    boolean disabled = false
}
