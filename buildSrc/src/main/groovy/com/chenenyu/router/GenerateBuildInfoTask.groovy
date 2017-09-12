package com.chenenyu.router

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Generate a java file to record all the modules.
 * <p>
 * Created by chenenyu on 2017/3/1.
 */
class GenerateBuildInfoTask extends DefaultTask {
    static final String BUILD_INFO_NAME = "RouterBuildInfo.java"

    File routerFolder
    File buildInfoFile
    String buildInfoContent

    GenerateBuildInfoTask() {
        group = 'router'
    }

    @TaskAction
    void generate() {
        project.logger.error(">>> ${project.name}: Generate ${BUILD_INFO_NAME} begin...")

        if (!routerFolder.exists()) {
            routerFolder.mkdirs()
        }
        routerFolder = new File(routerFolder, "com" + File.separator + "chenenyu" + File.separator + "router")
        routerFolder.mkdirs()
        buildInfoFile = new File(routerFolder, BUILD_INFO_NAME)
        if (!buildInfoFile.exists()) {
            buildInfoFile.createNewFile()
        }

        PrintWriter pw = new PrintWriter(buildInfoFile)
        pw.print(buildInfoContent)
        pw.flush()
        pw.close()

        println(buildInfoContent)

        project.logger.error("${project.name}: Generate ${BUILD_INFO_NAME} end. <<<")
    }

}