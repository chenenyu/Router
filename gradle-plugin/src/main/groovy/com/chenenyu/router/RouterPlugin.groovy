package com.chenenyu.router

import com.android.SdkConstants
import com.android.build.gradle.*
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessLibraryManifest
import com.android.build.gradle.tasks.ProcessTestManifest
import com.android.repository.Revision
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.TaskProvider

/**
 * Created by chenenyu on 2018/7/24.
 */
class RouterPlugin implements Plugin<Project> {
    private static final String APT_OPTION_MODULE_NAME = "moduleName"
    private static final String APT_OPTION_LOGGABLE = "loggable"

    String DEFAULT_ROUTER_RUNTIME_VERSION = "1.8.0"
    String DEFAULT_ROUTER_COMPILER_VERSION = "1.8.0"

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

        BaseExtension android = project.extensions.getByName('android')
        Map<String, String> options = [
                (APT_OPTION_MODULE_NAME): project.name,
                (APT_OPTION_LOGGABLE)   : loggable.toString(),
        ]
        android.defaultConfig.javaCompileOptions.annotationProcessorOptions.arguments(options)
        android.productFlavors.all { ProductFlavor flavor ->
            flavor.javaCompileOptions.annotationProcessorOptions.arguments(options)
        }

        // https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/main/kotlin/org/jetbrains/kotlin/gradle/plugin/KaptExtension.kt
        def kapt = project.extensions.findByName("kapt")
        if (kapt) {
            kapt.arguments({
                arg(APT_OPTION_MODULE_NAME, project.name)
                arg(APT_OPTION_LOGGABLE, loggable.toString())
            })
        }

        // com.android.Version added in 3.6.0, and com.android.builder.model.Version was deprecated in 3.6.0
        String agpVersion
        try {
            Configuration configuration = project.rootProject.buildscript.configurations.getByName(ScriptHandler.CLASSPATH_CONFIGURATION)
            agpVersion = configuration.resolvedConfiguration.firstLevelModuleDependencies.find {
                it.moduleGroup == 'com.android.tools.build' && it.moduleName == 'gradle'
            }.moduleVersion
        } catch (e) {
            project.logger.error(e.toString())
            return
//            agpVersion = Version.ANDROID_GRADLE_PLUGIN_VERSION
        }

        Revision revision = Revision.parseRevision(agpVersion, Revision.Precision.PREVIEW)
        if (revision.major > 4) {
            v7(project)
        } else if (revision.major == 4) {
            if (revision.minor >= 2) {
                v4_2(project)
            } else if (revision.minor == 1) {
                v4_1(project)
            } else {
                v3(project)
            }
        } else {
            v3(project)
        }
    }

    void v7(Project project) {
        def manifestClz = Class.forName('com.android.build.api.artifact.SingleArtifact$MERGED_MANIFEST')
        def instanceField = manifestClz.getField('INSTANCE')
        def artifactInstance = instanceField.get(null)
        // com.android.build.api.extension.AndroidComponentsExtension
        def androidComponents = project.extensions.getByName('androidComponents')
        androidComponents.onVariants(androidComponents.selector().all(), { variant -> // com.android.build.api.variant.Variant
            TaskProvider<ManifestTransformerTask> taskProvider = project.tasks.register(
                    "process${variant.name.capitalize()}RouterManifest", ManifestTransformerTask.class, project)
            variant.artifacts.use(taskProvider).wiredWithFiles({ it.mergedManifest },
                    { it.updatedManifest }).toTransform(/*SingleArtifact.MERGED_MANIFEST.INSTANCE*/ artifactInstance)
        })
    }

    void v4_2(Project project) {
        def manifestClz = Class.forName('com.android.build.api.artifact.ArtifactType$MERGED_MANIFEST')
        def instanceField = manifestClz.getField('INSTANCE')
        def artifactInstance = instanceField.get(null)
        // com.android.build.api.extension.AndroidComponentsExtension
        def androidComponents = project.extensions.getByName('androidComponents')
        androidComponents.onVariants(androidComponents.selector().all(), { variant -> // com.android.build.api.variant.Variant
            TaskProvider<ManifestTransformerTask> taskProvider = project.tasks.register(
                    "process${variant.name.capitalize()}RouterManifest", ManifestTransformerTask.class, project)
            variant.artifacts.use(taskProvider).wiredWithFiles({ it.mergedManifest },
                    { it.updatedManifest }).toTransform(/*ArtifactType.MERGED_MANIFEST.INSTANCE*/ artifactInstance)
        })
    }

    void v4_1(Project project) {
        /// BaseAppModuleExtension/LibraryExtension
        def android = project.extensions.getByName('android')
        if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(DynamicFeaturePlugin)) {
            // Way1:
//            AppExtension app = android
//            app.applicationVariants.all { ApplicationVariantImpl variant -> // com.android.build.gradle.internal.api.ApplicationVariantImpl
//                variant.outputs.all { BaseVariantOutput output -> // com.android.build.gradle.api.BaseVariantOutput
//                    output.processManifestProvider.get().doLast { ProcessMultiApkApplicationManifest task ->
//                        File manifestOutputFile = task.multiApkManifestOutputDirectory.get().file(SdkConstants.ANDROID_MANIFEST_XML).asFile
//                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
//                    }
//                }
//            }

            // Way2: Only support app module
            def manifestClz = Class.forName('com.android.build.api.artifact.ArtifactType$MERGED_MANIFEST')
            def instanceField = manifestClz.getField('INSTANCE')
            def artifactInstance = instanceField.get(null)

            android.onVariantProperties { /*VariantPropertiesImpl*/ variant ->
                // ApplicationVariantPropertiesImpl/LibraryVariantPropertiesImpl
                TaskProvider<ManifestTransformerTask> taskProvider = project.tasks.register(
                        "process${variant.name.capitalize()}RouterManifest", ManifestTransformerTask.class, project)
                // variant.artifacts: com.android.build.api.artifact.Artifacts
                variant.artifacts.use(taskProvider).wiredWithFiles({ it.mergedManifest },
                        { it.updatedManifest }).toTransform(/*ArtifactType.MERGED_MANIFEST.INSTANCE*/ artifactInstance)
            }
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            LibraryExtension lib = android
            lib.libraryVariants.all { LibraryVariantImpl variant ->
                variant.outputs.all { /*BaseVariantOutput*/ output -> // com.android.build.gradle.api.BaseVariantOutput
                    output.processManifestProvider.get().doLast { ProcessLibraryManifest task ->
                        File manifestOutputFile = task.manifestOutputFile.get().asFile
                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
                    }
                }
            }
        } else if (project.plugins.hasPlugin(TestPlugin)) {
            TestExtension test = android
            test.applicationVariants.all { variant ->
                it.outputs.all { /*BaseVariantOutput*/ output -> // com.android.build.gradle.api.BaseVariantOutput
                    output.processManifestProvider.get().doLast { ProcessTestManifest task ->
                        File manifestOutputFile = task.packagedManifestOutputDirectory.get().file(SdkConstants.ANDROID_MANIFEST_XML).asFile
                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
                    }
                }
            }
        }
    }

    void v3(Project project) {
        def android = project.extensions.getByName('android')
        if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(DynamicFeaturePlugin)) {
            AppExtension app = android
            app.applicationVariants.all { ApplicationVariantImpl variant -> // com.android.build.gradle.internal.api.ApplicationVariantImpl
                variant.outputs.all { /*BaseVariantOutput*/ output -> // com.android.build.gradle.api.BaseVariantOutput
                    output.processManifestProvider.get().doLast { ProcessApplicationManifest task ->
                        File manifestOutputFile = task.manifestOutputDirectory.get().file(SdkConstants.ANDROID_MANIFEST_XML).asFile
                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
                    }
                }
            }
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            LibraryExtension lib = android
            lib.libraryVariants.all { LibraryVariantImpl variant ->
                variant.outputs.all { /*BaseVariantOutput*/ output -> // com.android.build.gradle.api.BaseVariantOutput
                    output.processManifestProvider.get().doLast { ProcessLibraryManifest task ->
                        File manifestOutputFile = task.manifestOutputFile.get().asFile
                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
                    }
                }
            }
        } else if (project.plugins.hasPlugin(TestPlugin)) {
            TestExtension test = android
            test.applicationVariants.all { variant ->
                it.outputs.all { /*BaseVariantOutput*/ output -> // com.android.build.gradle.api.BaseVariantOutput
                    output.processManifestProvider.get().doLast { ProcessTestManifest task ->
//                        File manifestOutputFolder = Strings.isNullOrEmpty(task.apkData.getDirName())
//                                ? task.manifestOutputDirectory.get().asFile
//                                : task.manifestOutputDirectory.get().file(task.apkData.getDirName()).asFile
//                        File manifestOutputFile = new File(manifestOutputFolder, SdkConstants.ANDROID_MANIFEST_XML)
                        File manifestOutputFile = task.manifestOutputDirectory.get().file(SdkConstants.ANDROID_MANIFEST_XML).asFile
                        ManifestTransformer.transform(project, manifestOutputFile, manifestOutputFile)
                    }
                }
            }
        }
    }
}
