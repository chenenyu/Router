package com.chenenyu.router

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.utils.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * <p>
 * Created by Cheney on 2017/1/10.
 */
class RouterPlugin implements Plugin<Project> {
    static final String APT_OPTION_NAME = "moduleName"

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
            String routerVersion = "1.1.0"
            String compilerVersion = "0.6.0"
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("routerVersion")) {
                routerVersion = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                compilerVersion = ext.get("compilerVersion")
            }

            // compat for plugin: android-apt
            String apt = "annotationProcessor"
            if (project.plugins.hasPlugin("android-apt")) {
                apt = "apt"
            }

            project.dependencies.add("compile", "com.chenenyu.router:router:${routerVersion}")
            project.dependencies.add(apt, "com.chenenyu.router:compiler:${compilerVersion}")
        }


        String validModuleName = project.name.replace('.', '_').replace('-', '_')

        project.afterEvaluate {
            project.rootProject.subprojects.each {
                if (it.plugins.hasPlugin(AppPlugin) && !it.plugins.hasPlugin(RouterPlugin)) {
                    project.logger.error("Have you forgotten to apply plugin 'com.chenenyu.router' " +
                            "in module: '${it.name}'?")
                }
            }

            if (project.plugins.hasPlugin(AppPlugin)) {
                // Read template in advance, it can't be read in GenerateBuildInfoTask for some unknown reason.
                String template
                InputStream is = RouterPlugin.class.getResourceAsStream("/RouterBuildInfo.template")
                new Scanner(is).with {
                    template = it.useDelimiter("\\A").next()
                }
                File routerFolder = FileUtils.join(project.buildDir, "generated", "source", "router")

                ((AppExtension) project.android).applicationVariants.all { ApplicationVariantImpl variant ->

                    Set<Project> libs = project.rootProject.subprojects.findAll {
                        it.plugins.hasPlugin(LibraryPlugin) && it.plugins.hasPlugin(RouterPlugin)
                    }
                    StringBuilder sb = new StringBuilder();
                    if (!libs.empty) {
                        libs.each { Project p ->
                            sb.append(p.name.replace('.', '_').replace('-', '_')).append(",")
                        }
                    }
                    sb.append(validModuleName)

                    Task generateTask = project.tasks.create("generate${variant.name.capitalize()}BuildInfo", GenerateBuildInfoTask) {
                        it.applicationVariant = variant
                        it.routerFolder = routerFolder
                        it.buildInfoContent = template.replaceAll("%ALL_MODULES%", sb.toString())
                    }

                    if (variant.javaCompile != null) {
                        variant.javaCompile.dependsOn generateTask
                        // add generated file to javac source
                        variant.javaCompile.source(routerFolder)

                        // Inspired by com.android.build.gradle.tasks.factory.JavaCompileConfigAction
                        // javac apt
                        variant.javaCompile.options.compilerArgs.add("-A${APT_OPTION_NAME}=${validModuleName}")
                    }
                }
            } else {
                ((LibraryExtension) project.android).libraryVariants.all { LibraryVariantImpl variant ->
                    if (variant.javaCompile != null) {
                        variant.javaCompile.options.compilerArgs.add("-A${APT_OPTION_NAME}=${validModuleName}")
                    }
                }
            }
        }
    }

}