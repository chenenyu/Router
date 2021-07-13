package com.chenenyu.router

import com.android.build.gradle.*
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * Created by chenenyu on 2018/7/24.
 */
class RouterPlugin implements Plugin<Project> {
    private static final String APT_OPTION_MODULE_NAME = "moduleName"
    private static final String APT_OPTION_LOGGABLE = "loggable"

    String DEFAULT_ROUTER_RUNTIME_VERSION = "1.7.5"
    String DEFAULT_ROUTER_COMPILER_VERSION = "1.7.5"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)                    // com.android.application
                && !project.plugins.hasPlugin(LibraryPlugin)         // com.android.library
                && !project.plugins.hasPlugin(TestPlugin)            // com.android.test
                && !project.plugins.hasPlugin(DynamicFeaturePlugin)) // com.android.dynamic-feature, added in 3.2
        {
            throw new GradleException("android plugin required.")
        }

        // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
        ExtraPropertiesExtension ext = project.rootProject.ext

        // kotlin project ?
        // https://github.com/JetBrains/kotlin/tree/master/libraries/tools/kotlin-gradle-plugin/src/main/resources/META-INF/gradle-plugins
        def isKotlinProject = project.plugins.hasPlugin('kotlin-android') || project.plugins.hasPlugin('org.jetbrains.kotlin.android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt') && !project.plugins.hasPlugin('org.jetbrains.kotlin.kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        String aptConf = 'annotationProcessor'
        if (isKotlinProject) {
            aptConf = 'kapt'
        }

        // Add dependencies
        Project routerProject = project.rootProject.findProject("router")
        Project compilerProject = project.rootProject.findProject("compiler")
        if (routerProject && compilerProject) { // local
            project.dependencies.add('implementation', routerProject)
            project.dependencies.add(aptConf, compilerProject)
        } else {
            if (ext.has("routerVersion")) {
                DEFAULT_ROUTER_RUNTIME_VERSION = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                DEFAULT_ROUTER_COMPILER_VERSION = ext.get("compilerVersion")
            }
            project.dependencies.add('implementation',
                    "com.chenenyu.router:router:${DEFAULT_ROUTER_RUNTIME_VERSION}")
            project.dependencies.add(aptConf,
                    "com.chenenyu.router:compiler:${DEFAULT_ROUTER_COMPILER_VERSION}")
        }

        boolean loggable = true
        if (ext.has('compilerLoggable')) {
            loggable = ext.get('compilerLoggable')
        }

        BaseExtension android = project.extensions.findByName("android")
        if (android) {
            Map<String, String> options = [
                    (APT_OPTION_MODULE_NAME): project.name,
                    (APT_OPTION_LOGGABLE)   : loggable.toString(),
            ]
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.arguments(options)
            android.productFlavors.all { ProductFlavor flavor ->
                flavor.javaCompileOptions.annotationProcessorOptions.arguments(options)
            }
        }

        // https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/main/kotlin/org/jetbrains/kotlin/gradle/plugin/KaptExtension.kt
        def kapt = project.extensions.findByName("kapt")
        if (kapt) {
            kapt.arguments({
                arg(APT_OPTION_MODULE_NAME, project.name)
                arg(APT_OPTION_LOGGABLE, loggable.toString())
            })
        }

        if (project.plugins.hasPlugin(AppPlugin)) {
            def transform = new RouterTransform(project)
            android.registerTransform(transform)
        }
    }
}
