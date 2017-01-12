package com.chenenyu.router

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
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

        // Modify build config
        project.afterEvaluate {
            if (project.plugins.hasPlugin(AppPlugin)) {
                ((AppExtension) project.android).applicationVariants.all { ApplicationVariantImpl variant ->
                    // What the f**k, the flowing line wasted me some days.
                    // Inspired by com.android.build.gradle.tasks.factory.JavaCompileConfigAction.
                    // F**king source code!
                    variant.variantData.javacTask.options.compilerArgs.add("-AmoduleName=${project.name}")

                    Set<Project> libs = project.rootProject.subprojects.findAll {
                        it.plugins.hasPlugin(LibraryPlugin) && it.plugins.hasPlugin("com.chenenyu.router")
                    }
                    StringBuilder sb = new StringBuilder();
                    if (!libs.empty) {
                        libs.each { Project p ->
                            sb.append(p.name.replace('.', '_')).append(",")
                        }
                    }
                    sb.append(project.name.replace('.', '_'))
                    variant.buildConfigField("String", "MODULES_NAME", "\"$sb\"")
                }
            } else {
                ((LibraryExtension) project.android).libraryVariants.all { LibraryVariantImpl variant ->
                    variant.variantData.javacTask.options.compilerArgs.add("-AmoduleName=${project.name}")
                }
            }
        }

    }

}