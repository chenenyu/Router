package com.chenenyu.router

import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.utils.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * <p>
 * Created by Cheney on 2017/3/1.
 */
class GenerateBuildInfoTask extends DefaultTask {
    @Input
    ApplicationVariantImpl applicationVariant
    @Input
    File routerFolder
    File buildInfoFile
    @Input
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
        routerFolder = FileUtils.join(routerFolder, "com", "chenenyu", "router")
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