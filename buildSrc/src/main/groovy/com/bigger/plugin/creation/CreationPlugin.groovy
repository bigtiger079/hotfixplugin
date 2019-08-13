import com.bigger.plugin.creation.ModuleCreateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class CreationPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.register("ModuleCreateTask", ModuleCreateTask.class)
    }
}