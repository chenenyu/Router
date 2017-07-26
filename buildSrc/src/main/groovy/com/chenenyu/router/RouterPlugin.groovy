package com.chenenyu.router

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * <p>
 * Created by Cheney on 2017/1/10.
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

        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        // Add annotationProcessorOptions
        def android = project.extensions.android // BaseExtension
        android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
        android.productFlavors.all {
            it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
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
            String routerVersion = "1.2.4"
            String compilerVersion = "1.2.4"
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
                if (it.plugins.hasPlugin(APP) && !it.plugins.hasPlugin(RouterPlugin)) {
                    project.logger.error("Have you forgotten to apply plugin: 'com.chenenyu.router'" +
                            " in module: '${it.name}'?")
                }
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

                // Record lib modules' name
                StringBuilder sb = new StringBuilder()
                Set<Project> subs = project.rootProject.subprojects
                subs.each {
                    if (it.plugins.hasPlugin(LIBRARY) && it.plugins.hasPlugin(RouterPlugin)) {
                        sb.append(it.name.replace('.', '_').replace('-', '_')).append(",")
                    }
                }
                String validAppName = project.name.replace('.', '_').replace('-', '_')
                sb.append(validAppName)

                project.android.applicationVariants.all { variant ->
                    // Create task
                    Task generateTask = project.tasks.create("generate${variant.name.capitalize()}BuildInfo", GenerateBuildInfoTask) {
                        it.routerFolder = routerFolder
                        it.buildInfoContent = template.replaceAll("%ALL_MODULES%", sb.toString())
                    }

                    // Add generated file to javac source
                    variant.javaCompile.source(routerFolder)
                    variant.javaCompile.dependsOn generateTask
                }
            }
        }
    }

}