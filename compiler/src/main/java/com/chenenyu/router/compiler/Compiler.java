package com.chenenyu.router.compiler;

import com.chenenyu.router.annotation.Route;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public class Compiler extends AbstractProcessor {
    private Elements elementUtils = null;
    private Filer filer = null;
    private Messager messager = null;
    private Types typeUtils = null;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Route.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        // 合法的TypeElement集合
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            // 检查被注解为@Route的元素是否是一个类
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "The annotated element is not a class, but a %s",
                        element.getSimpleName().toString());
                return true;
            }
            try {
                if (!isValidClass((TypeElement) element)) {
                    continue;
                }
                typeElements.add((TypeElement) element);
            } catch (RouteException e) {
                error(e.getElement(), e.getMessage());
                return true;
            }
        }
        generateCode(typeElements);
        return true;
    }

    /**
     * Verify the annotated class.
     *
     * @param typeElement TypeElement
     * @return True if legal, false otherwise.
     * @throws RouteException Illegal annotation.
     */
    private boolean isValidClass(TypeElement typeElement) throws RouteException {
        Set<Modifier> modifiers = typeElement.getModifiers();
        // non-public class, error.
        if (!modifiers.contains(Modifier.PUBLIC)) {
            throw new RouteException(String.format("The class %s is not be public.",
                    typeElement.getQualifiedName()), typeElement);
        }
        // abstract class, skip.
        if (modifiers.contains(Modifier.ABSTRACT)) {
            error(typeElement, "The class %s is abstract. You can't annotate abstract classes with @%s",
                    typeElement.getQualifiedName().toString(), Route.class.getSimpleName());
            return false;
        }
        // not an activity or a service, error.
//        if (!veritySuperClass(typeElement, "android.app.Activity")
//                || !veritySuperClass(typeElement, "android.app.Service")) {
//            throw new RouteException(String.format("The class %s is not an Activity or a Service.",
//                    typeElement.getQualifiedName()), typeElement);
//        }
        return true;
    }

    /**
     * Verify an Activity or a Service.
     *
     * @param type       TypeElement
     * @param superClass Super class name.
     * @return True if verified, false otherwise.
     */
    private boolean veritySuperClass(TypeElement type, String superClass) {
        return !(type == null || "java.lang.Object".equals(type.getQualifiedName().toString()))
                && (type.getQualifiedName().toString().equals(superClass)
                || veritySuperClass((TypeElement) typeUtils.asElement(
                type.getSuperclass()), superClass));
    }

    private void error(Element element, String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    /**
     * Generate a .java file that contains route map.
     */
    private void generateCode(Set<TypeElement> elements) {
        TypeElement activityType = elementUtils.getTypeElement("android.app.Activity");
        // Map<String, Class<? extends Activity>> map
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(activityType))));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "map").build();

        MethodSpec.Builder initActivityTable = MethodSpec.methodBuilder("initActivityTable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapParameterSpec);
        for (TypeElement element : elements) {
            Route route = element.getAnnotation(Route.class);
            String[] paths = route.value();
            for (String path : paths) {
                initActivityTable.addStatement("map.put($S, $T.class)", path, ClassName.get(element));
            }
        }
        TypeElement routeTableType = elementUtils.getTypeElement("com.chenenyu.router.RouteTable");
        TypeSpec type = TypeSpec.classBuilder("AnnotatedRouteTable")
                .addSuperinterface(ClassName.get(routeTableType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initActivityTable.build())
                .build();
        try {
            JavaFile.builder("com.chenenyu.router", type).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
