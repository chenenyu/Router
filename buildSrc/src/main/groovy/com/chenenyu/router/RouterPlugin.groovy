package com.chenenyu.router

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <p>
 * Created by Cheney on 2017/1/10.
 */
class RouterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!(project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin))) {
            throw new IllegalArgumentException(
                    'Router gradle plugin can only be applied to android projects.')
        }

        // Add dependencies
        Project router = project.rootProject.findProject("router")
        Project compiler = project.rootProject.findProject("compiler")
        if (router && compiler) {
            project.dependencies {
                compile router
                annotationProcessor compiler
            }
        } else {
            project.dependencies.add("compile", "com.chenenyu.router:router:latest.integration")
            project.dependencies.add("annotationProcessor", "com.chenenyu.router:compiler:latest.integration")
        }

        // Modify BuildConfig
        project.afterEvaluate {
            boolean isApp = project.plugins.hasPlugin(AppPlugin)
            project.android.buildTypes.all { BuildType buildType ->
                buildType.buildConfigField("boolean", "IS_APP", Boolean.toString(isApp))
                if (isApp) { // com.android.application
                    Set<Project> libs = project.rootProject.subprojects.findAll {
                        it.plugins.hasPlugin(LibraryPlugin)
                    }
                    if (!libs.empty) {
                        StringBuilder sb = new StringBuilder();
                        libs.each { Project p ->
                            sb.append(p.name.replace('.', '_')).append(",")
                        }
                        String modules = sb.substring(0, sb.length() - 1)
                        buildType.buildConfigField("String", "MODULES_NAME", "\"$modules\"")
                    }
                } else { // com.android.library
                    buildType.buildConfigField("String", "MODULE_NAME", "\"$project.name\"")
                }
            }
        }

    }

}