package com.chenenyu.router

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by chenenyu on 2018/7/26.
 */
class Knife {
    static void handle() {
        File targetFile = RouterTransform.registerTargetFile
        assert targetFile != null && targetFile.exists()

        if (targetFile.name.endsWith(".jar")) {
            def optJar = new File(targetFile.getParent(), targetFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def jarFile = new JarFile(targetFile)
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
            Enumeration enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(entryName) // new entry
                jarOutputStream.putNextEntry(zipEntry)
                jarFile.getInputStream(jarEntry).withCloseable { is ->
                    if (entryName == Scanner.REGISTER_CLASS_NAME) { // find AptHub.class
                        def bytes = modifyClass(is)
                        jarOutputStream.write(bytes)
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(is))
                    }
                    jarOutputStream.closeEntry()
                }
            }
            jarOutputStream.close()
            jarFile.close()

            targetFile.delete()
            optJar.renameTo(targetFile)
        } else if (targetFile.name.endsWith(".class")) { // 一般不会走到这里，因为AptHub位于jar包中
            modifyClass(new FileInputStream(targetFile))
        }
    }

    private static byte[] modifyClass(InputStream inputStream) {
        inputStream.withCloseable { is ->
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(cr, 0)
            ClassVisitor cv = new AptClassVisitor(cw)
            cr.accept(cv, 0)
            return cw.toByteArray()
        }
    }

    /**
     * Delegate static code block
     */
    private static class AptClassVisitor extends ClassVisitor {
        AptClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == "<clinit>") {
                mv = new ClinitMethodVisitor(mv)
            }
            return mv
        }
    }

    private static class ClinitMethodVisitor extends MethodVisitor {
        ClinitMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        /**
         * Java code:<br>
         * <code>
         * static {<br>
         *     new AppRouteTable().handle(routeTable);<br>
         *     new AppInterceptorTable().handle(interceptorTable);<br>
         *     new AppTargetInterceptorsTable().handle(targetInterceptorsTable);<br>
         *     // other modules' table...<br>
         *}<br>
         * </code>
         * ASM code:<br>
         * <code>
         *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppRouteTable");<br>
         *     mv.visitInsn(Opcodes.DUP);<br>
         *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppRouteTable", "<init>", "()V", false);<br>
         *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "routeTable", "Ljava/util/Map;");<br>
         *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppRouteTable", "handle", "(Ljava/util/Map;)V", false);<br>
         *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppInterceptorTable");<br>
         *     mv.visitInsn(Opcodes.DUP);<br>
         *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppInterceptorTable", "<init>", "()V", false);<br>
         *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "interceptorTable", "Ljava/util/Map;");<br>
         *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppInterceptorTable", "handle", "(Ljava/util/Map;)V", false);<br>
         *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppTargetInterceptorsTable");<br>
         *     mv.visitInsn(Opcodes.DUP);<br>
         *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppTargetInterceptorsTable", "<init>", "()V", false);<br>
         *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "targetInterceptorsTable", "Ljava/util/Map;");<br>
         *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppTargetInterceptorsTable", "handle", "(Ljava/util/Map;)V", false);<br>
         *     mv.visitInsn(Opcodes.RETURN);<br>
         * </code>
         */
        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) { // handle init code before return
                Scanner.records.each { record ->
                    record.aptClasses.each { className ->
                        println("router: handle $className")
                        mv.visitTypeInsn(Opcodes.NEW, className)
                        mv.visitInsn(Opcodes.DUP)
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false)
                        mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/AptHub", getFieldNameByInterface(record), "Ljava/util/Map;")
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "handle", "(Ljava/util/Map;)V", false)
                    }
                }
            }
            super.visitInsn(opcode)
        }

        /**
         * @return AptHub中用于存储table的类变量名字
         */
        synchronized String getFieldNameByInterface(Record record) {
            if (record.templateName == Scanner.TEMPLATE_ROUTE_TABLE) {
                return "routeTable"
            } else if (record.templateName == Scanner.TEMPLATE_INTERCEPTOR_TABLE) {
                return "interceptorTable"
            } else if (record.templateName == Scanner.TEMPLATE_TARGET_INTERCEPTORS_TABLE) {
                return "targetInterceptorsTable"
            }
            throw IllegalArgumentException("Unrecognized record[${record.templateName}]")
        }
    }
}
