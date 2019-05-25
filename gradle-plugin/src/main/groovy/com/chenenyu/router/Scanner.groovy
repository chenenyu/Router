package com.chenenyu.router

import com.android.build.api.transform.JarInput
import com.google.common.collect.ImmutableList
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by chenenyu on 2018/7/26.
 */
class Scanner {
    static final String TEMPLATE_ROUTE_TABLE = "com/chenenyu/router/template/RouteTable"
    static final String TEMPLATE_INTERCEPTOR_TABLE = "com/chenenyu/router/template/InterceptorTable"
    static
    final String TEMPLATE_TARGET_INTERCEPTORS_TABLE = "com/chenenyu/router/template/TargetInterceptorsTable"

    static final List<Record> records = ImmutableList.of(
            new Record(TEMPLATE_ROUTE_TABLE),
            new Record(TEMPLATE_INTERCEPTOR_TABLE),
            new Record(TEMPLATE_TARGET_INTERCEPTORS_TABLE))

    static final String REGISTER_CLASS_NAME = "com/chenenyu/router/AptHub.class"

    private static final String APT_CLASS_PACKAGE_NAME = "com/chenenyu/router/apt"

    private static
    final Set<String> excludeJar = ["com.android.support", "android.arch.", "androidx."]

    static void clearRecordsClasses() {
        records.forEach { record ->
            record.aptClasses.clear()
        }
    }

    static boolean shouldScanJar(JarInput jarInput) {
        excludeJar.each {
            if (jarInput.name.contains(it))
                return false
        }
        return true
    }

    static boolean shouldScanClass(File classFile) {
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(APT_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描jar包
     */
    static void scanJar(File src, File dest) {
        if (src && src.exists()) {
            def jar = new JarFile(src)
            Enumeration enumeration = jar.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName == REGISTER_CLASS_NAME) {
                    // mark
                    RouterTransform.registerTargetFile = dest
                } else if (entryName.startsWith(APT_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                }
            }
            jar.close()
        }
    }

    static void scanClass(File classFile) {
        scanClass(new FileInputStream(classFile))
    }

    /**
     * 扫描class
     */
    static void scanClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            ScanClassVisitor cv = new ScanClassVisitor()
            cr.accept(cv, 0)
        }
    }

    static class ScanClassVisitor extends ClassVisitor {
        ScanClassVisitor() {
            super(Opcodes.ASM5)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            if (interfaces != null) {
                records.each { record ->
                    interfaces.each { interfaceName ->
                        if (interfaceName == record.templateName) {
                            record.aptClasses.add(name)
                        }
                    }
                }
            }
        }
    }
}
