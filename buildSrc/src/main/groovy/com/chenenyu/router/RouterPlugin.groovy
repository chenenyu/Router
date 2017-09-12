package com.chenenyu.router

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * Router gradle plugin.
 * <p>
 * Created by chenenyu on 2017/1/10.
 */
class RouterPlugin implements Plugin<Project> {
    static final String APP = "com.android.application"
    static final String LIBRARY = "com.android.library"
    static final String APT_OPTION_NAME = "moduleName"

    @Override
    void apply(Project project) {
        if (!(project.plugins.hasPlugin(APP) || project.plugins.hasPlugin(LIBRARY))) {
            throw new IllegalArgumentException(
                    'Router gradle plugin can only be applied to android projects.')
        }

        // Add annotationProcessorOptions
        def android = project.extensions.android // BaseExtension
        android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
        android.productFlavors.all {
            it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
        }

        // Kotlin project ?
        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        // Add dependencies
        Project router = project.rootProject.findProject("router")
        Project routerCompiler = project.rootProject.findProject("compiler")
        if (router && routerCompiler) { // local
            project.dependencies {
                compile router
                if (isKotlinProject) {
                    kapt routerCompiler
                } else {
                    annotationProcessor routerCompiler
                }
            }
        } else { // remote
            String routerVersion = "1.2.5"
            String compilerVersion = "1.2.5"
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


        if (project.plugins.hasPlugin(APP)) {
            // Read template in advance, it can't be read in GenerateBuildInfoTask.
            InputStream is = RouterPlugin.class.getResourceAsStream("/RouterBuildInfo.template")
            String template
            new Scanner(is).with {
                template = it.useDelimiter("\\A").next()
            }
            File routerFolder = new File(project.buildDir,
                    "generated" + File.separator + "source" + File.separator + "router")

            project.afterEvaluate {
                // Record router modules' name, include library and app modules.
                StringBuilder sb = new StringBuilder()
                Set<Project> subs = project.rootProject.subprojects
                subs.findAll {
                    it.plugins.hasPlugin(LIBRARY) && it.plugins.hasPlugin(RouterPlugin)
                }.each {
                    sb.append(validateName(it.name)).append(",") // library
                }
                sb.append(validateName(project.name)) // app

                android.applicationVariants.all { variant ->
                    // Create task
                    Task generateTask = project.tasks.create(
                            "generate${variant.name.capitalize()}BuildInfo", GenerateBuildInfoTask) {
                        it.routerFolder = routerFolder
                        it.buildInfoContent = template.replaceAll("%ALL_MODULES%", sb.toString())
                    }
                    // Add generated file to javac source
                    Task javac = variant.javaCompile
                    javac.source(routerFolder)
                    generateTask.dependsOn javac.taskDependencies.getDependencies(javac)
                    javac.dependsOn generateTask
                }
            }
        }
    }

    static String validateName(String moduleName) {
        moduleName.replace('.', '_').replace('-', '_')
    }

}