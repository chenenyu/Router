package com.chenenyu.router

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

abstract class ManifestTransformerTask extends DefaultTask {
    private Project project

    @Inject
    ManifestTransformerTask(Project project) {
        this.project = project
    }

    @InputFile
    abstract RegularFileProperty getMergedManifest()

    @OutputFile
    abstract RegularFileProperty getUpdatedManifest()

    @TaskAction
    void taskAction() {
        File input = getMergedManifest().get().asFile
        File output = getUpdatedManifest().get().asFile
//        project.logger.warn("input: ${input.absolutePath}")
//        project.logger.warn("output: ${output.absolutePath}")

        ManifestTransformer.transform(project, input, output)
    }
}
