package com.chenenyu.router

//import groovy.xml.XmlParser // Added in gradle7.0(groovy 3.0.0)
import groovy.xml.XmlUtil
import org.gradle.api.Project

class ManifestTransformer {
    static void transform(Project project, File input, File output) {
        Node xml = new XmlParser().parse(input)
        Node applicationNode = xml.get('application')[0]
        applicationNode.appendNode('meta-data', ['android:name': project.name, 'android:value': 'com.chenenyu.router.moduleName'])
        def result = XmlUtil.serialize(xml)
        output.write(result)
    }
}