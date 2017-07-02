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

        // project.logger.error("----- Router begin: ${project.name}-----")

        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        // Add dependencies
        Project router = project.rootProject.findProject("router")
        Project routerCompiler = project.rootProject.findProject("compiler")
        if (router && routerCompiler) {
            project.dependencies {
                compile router
                if (isKotlinProject) {
                    kapt routerCompiler
                } else {
                    annotationProcessor routerCompiler
                }
            }
        } else {
            String routerVersion = "1.2.1"
            String compilerVersion = "1.2.1"
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("routerVersion")) {
                routerVersion = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                compilerVersion = ext.get("compilerVersion")
            }

            // compat for plugin: android-apt kotlin-kapt
            String apt = "annotationProcessor"
            if (project.plugins.hasPlugin("android-apt")) {
                apt = "apt"
            } else if (isKotlinProject) {
                apt = 'kapt'
            }

            project.dependencies.add("compile", "com.chenenyu.router:router:${routerVersion}")
            project.dependencies.add(apt, "com.chenenyu.router:compiler:${compilerVersion}")
        }

        project.afterEvaluate {
            project.rootProject.subprojects.each {
                if (it.plugins.hasPlugin(AppPlugin) && !it.plugins.hasPlugin(RouterPlugin)) {
                    project.logger.error("Have you forgotten to apply plugin 'com.chenenyu.router' " +
                            "in module: '${it.name}'?")
                }
            }

            if (project.plugins.hasPlugin(AppPlugin)) {
                // Read template in advance, it can't be read in GenerateBuildInfoTask.
                InputStream is = RouterPlugin.class.getResourceAsStream("/RouterBuildInfo.template")
                String template
                new Scanner(is).with {
                    template = it.useDelimiter("\\A").next()
                }
                File routerFolder = FileUtils.join(project.buildDir, "generated", "source", "router")

                // Record lib modules' name
                StringBuilder sb = new StringBuilder()
                Set<Project> subs = project.rootProject.subprojects
                subs.each {
                    if (it.plugins.hasPlugin(LibraryPlugin) && it.plugins.hasPlugin(RouterPlugin)) {
                        sb.append(it.name.replace('.', '_').replace('-', '_')).append(",")
                    }
                }
                String validAppName = project.name.replace('.', '_').replace('-', '_')
                sb.append(validAppName)

                ((AppExtension) project.android).applicationVariants.all { ApplicationVariantImpl variant ->
                    // Create task
                    Task generateTask = project.tasks.create("generate${variant.name.capitalize()}BuildInfo", GenerateBuildInfoTask) {
                        it.applicationVariant = variant
                        it.routerFolder = routerFolder
                        it.buildInfoContent = template.replaceAll("%ALL_MODULES%", sb.toString())
                    }

                    variant.javaCompile.dependsOn generateTask
                    // Add generated file to javac source
                    variant.javaCompile.source(routerFolder)

                    if (!isKotlinProject) {
                        // Inspired by com.android.build.gradle.tasks.factory.JavaCompileConfigAction
                        variant.javaCompile.options.compilerArgs.add("-A${APT_OPTION_NAME}=${project.name}")
                    }
                }
            } else {
                ((LibraryExtension) project.android).libraryVariants.all { LibraryVariantImpl variant ->
                    if (!isKotlinProject) {
                        // Inspired by com.android.build.gradle.tasks.factory.JavaCompileConfigAction
                        variant.javaCompile.options.compilerArgs.add("-A${APT_OPTION_NAME}=${project.name}")
                    }
                }
            }
        }
    }

}