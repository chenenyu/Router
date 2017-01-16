package com.chenenyu.router

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

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
            String routerVersion = "latest.integration"
            String compilerVersion = "latest.integration"
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("routerVersion")) {
                routerVersion = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                compilerVersion = ext.get("compilerVersion")
            }
            project.dependencies.add("compile", "com.chenenyu.router:router:${routerVersion}")
            project.dependencies.add("annotationProcessor", "com.chenenyu.router:compiler:${compilerVersion}")
        }

        // Modify build config
        String validModuleName = project.name.replace('.', '_')
        project.afterEvaluate {
            if (project.plugins.hasPlugin(AppPlugin)) {
                ((AppExtension) project.android).applicationVariants.all { ApplicationVariantImpl variant ->
                    // What the f**k, the flowing line wasted me some days.
                    // Inspired by com.android.build.gradle.tasks.factory.JavaCompileConfigAction.
                    // F**king source code!
                    variant.variantData.javacTask.options.compilerArgs.add("-AmoduleName=${validModuleName}")

                    Set<Project> libs = project.rootProject.subprojects.findAll {
                        it.plugins.hasPlugin(LibraryPlugin) && it.plugins.hasPlugin(RouterPlugin)
                    }
                    StringBuilder sb = new StringBuilder();
                    if (!libs.empty) {
                        libs.each { Project p ->
                            sb.append(p.name.replace('.', '_')).append(",")
                        }
                    }
                    sb.append(validModuleName)
                    variant.buildConfigField("String", "MODULES_NAME", "\"$sb\"")
                }
            } else {
                ((LibraryExtension) project.android).libraryVariants.all { LibraryVariantImpl variant ->
                    variant.variantData.javacTask.options.compilerArgs.add("-AmoduleName=${validModuleName}")
                }
            }
        }

    }

}