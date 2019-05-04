import org.gradle.api.Project

import javax.inject.Inject

class TemplateCopy implements Runnable {
//    final Project project
    final String applicationId
    final String packageName
    final String moduleName
    @Inject
    TemplateCopy(String applicationId, String packageName, String moduleName) {
        this.applicationId = applicationId
        this.packageName = packageName
        this.moduleName = moduleName
    }
    @Override
    void run() {
//        def ModulesConfig = project.copySpec{
//            includeEmptyDirs = true
//            from 'template'
//            exclude "build", "*.class", "*.iml", "src/main/java/*"
//        }
//
//        def binding = [applicationId:applicationId, packageName:packageName, apkName:moduleName+".apk"]
//        project.copy {
//            expand binding
//            with ModulesConfig
//            into name
//        }
    }
}