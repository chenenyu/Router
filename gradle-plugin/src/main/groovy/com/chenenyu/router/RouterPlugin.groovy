package com.chenenyu.router

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * Created by chenenyu on 2018/7/24.
 */
class RouterPlugin implements Plugin<Project> {
    static final String APT_OPTION_NAME = "moduleName"

    String DEFAULT_ROUTER_RUNTIME_VERSION = "1.7.1"
    String DEFAULT_ROUTER_COMPILER_VERSION = "1.7.1"

    String androidBuildGradleVersion

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)                                // AppPlugin
                && !project.plugins.hasPlugin(LibraryPlugin)                     // LibraryPlugin
                && !project.plugins.hasPlugin(TestPlugin)                        // TestPlugin
                && !project.plugins.hasPlugin("com.android.instantapp")       // InstantAppPlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.feature")          // FeaturePlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.dynamic-feature")) // DynamicFeaturePlugin, added in 3.2
        {
            throw new GradleException("android plugin required.")
        }

        project.rootProject.buildscript.configurations.each {
            if (it.name == ScriptHandler.CLASSPATH_CONFIGURATION) { // classpath
                it.resolvedConfiguration.firstLevelModuleDependencies.each {
                    // println("${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}")
                    if (it.moduleGroup == "com.android.tools.build" && it.moduleName == "gradle") {
                        androidBuildGradleVersion = it.moduleVersion
                    }
                }
            }
        }
        if (!androidBuildGradleVersion) {
            throw new IllegalArgumentException("Unknown android build gradle plugin version.")
        }

        // kotlin project ?
        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        String compileConf = 'implementation'
        if (!is3_xVersion()) {
            compileConf = 'compile'
        }
        String aptConf = 'annotationProcessor'
        if (isKotlinProject) {
            aptConf = 'kapt'
        }

        // Add dependencies
        Project routerProject = project.rootProject.findProject("router")
        Project compilerProject = project.rootProject.findProject("compiler")
        if (routerProject && compilerProject) { // local
            project.dependencies.add(compileConf, routerProject)
            project.dependencies.add(aptConf, compilerProject)
        } else {
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("routerVersion")) {
                DEFAULT_ROUTER_RUNTIME_VERSION = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                DEFAULT_ROUTER_COMPILER_VERSION = ext.get("compilerVersion")
            }
            project.dependencies.add(compileConf,
                    "com.chenenyu.router:router:${DEFAULT_ROUTER_RUNTIME_VERSION}")
            project.dependencies.add(aptConf,
                    "com.chenenyu.router:compiler:${DEFAULT_ROUTER_COMPILER_VERSION}")
        }

        def android = project.extensions.findByName("android")
        if (android) {
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            android.productFlavors.all {
                it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            }
        }

        if (project.plugins.hasPlugin(AppPlugin)) {
            def transform = new RouterTransform(project)
            android.registerTransform(transform)
        }
    }

    /**
     * Whether the android gradle plugin version is 3.x
     */
    boolean is3_xVersion() {
        return androidBuildGradleVersion.split("\\.")[0].toInteger() >= 3
    }
}
