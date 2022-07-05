package com.chenenyu.router

//import groovy.xml.XmlParser // Added in gradle7.0(groovy 3.0.0)
import org.gradle.api.Project

class ManifestTransformer {
    static void transform(Project project, File input, File output) {
        Node manifest = new XmlParser().parse(input)
        Node applicationNode = null
        Object application = manifest.get('application')
        if (application instanceof NodeList) {
            if (application.isEmpty()) { // There is no `application` node in AndroidManifest.xml
                applicationNode = manifest.appendNode("application", ['xmlns:android': 'http://schemas.android.com/apk/res/android'])
            } else {
                applicationNode = application.first()
            }
            applicationNode.appendNode('meta-data', ['android:name': project.name, 'android:value': 'com.chenenyu.router.moduleName'])
        }
        if (applicationNode != null) {
            FileWriter fileWriter = new FileWriter(output)
            XmlNodePrinter nodePrinter = new XmlNodePrinter(new PrintWriter(fileWriter))
            nodePrinter.setPreserveWhitespace(true)
            nodePrinter.print(manifest)
        }
    }
}