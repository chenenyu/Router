package com.chenenyu.router

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * <p>
 * Created by Cheney on 2017/3/1.
 */
class GenerateBuildInfoTask extends DefaultTask {
    File routerFolder
    File buildInfoFile
    String buildInfoContent
    final String buildInfoName = "RouterBuildInfo.java"

    GenerateBuildInfoTask() {
        group = 'router'
    }

    @TaskAction
    void generate() {
        if (!routerFolder.exists()) {
            routerFolder.mkdirs()
        }
        routerFolder = new File(routerFolder, "com" + File.separator + "chenenyu" + File.separator + "router")
        routerFolder.mkdirs()
        buildInfoFile = new File(routerFolder, buildInfoName)
        if (!buildInfoFile.exists()) {
            buildInfoFile.createNewFile()
        }

        PrintWriter pw = new PrintWriter(buildInfoFile)
        pw.print(buildInfoContent)
        pw.flush()
        pw.close()
    }

}