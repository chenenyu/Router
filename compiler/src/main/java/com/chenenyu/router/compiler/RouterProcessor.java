package com.chenenyu.router.compiler;

import com.chenenyu.router.annotation.Route;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Route annotation processor.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
@SupportedAnnotationTypes("com.chenenyu.router.annotation.Route")
public class RouterProcessor extends AbstractProcessor {
    private static final String OPTION_MODULE_NAME = "moduleName";
    private static final String OPTION_ALL_MODULES = "allModules";

    private Elements elementUtils = null;
    private Filer filer = null;
    private Messager messager = null;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.singleton(OPTION_MODULE_NAME);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    /**
     * This method will be called some times.
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        // 合法的TypeElement集合
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            try {
                validateElement(element);
                typeElements.add((TypeElement) element);
            } catch (RouteException e) {
                error(e.getElement(), e.getMessage());
                return true;
            }
        }
        String allModules = processingEnv.getOptions().get(OPTION_ALL_MODULES);
        if (allModules != null) {
            generateConfig(allModules);
        }
        String moduleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
        if (moduleName != null) {
            generateCode(combineClzName(moduleName), typeElements);
        } else {
            error(null, "No option %s passed to annotation processor.", OPTION_MODULE_NAME);
        }
        return true;
    }

    /**
     * Verify the annotated class.
     *
     * @param typeElement TypeElement
     * @throws RouteException Illegal annotation.
     */
    private void validateElement(Element typeElement) throws RouteException {
        // 检查被注解为@Route的元素是否是一个类
        if (typeElement.getKind() != ElementKind.CLASS) {
            throw new RouteException(String.format("The annotated element is not a class, but a %s.",
                    typeElement.getKind().name()), typeElement);
        }
        Set<Modifier> modifiers = typeElement.getModifiers();
        // non-public class, error.
        if (!modifiers.contains(Modifier.PUBLIC)) {
            throw new RouteException(String.format("The class %s is not be public.",
                    ((TypeElement) typeElement).getQualifiedName()), typeElement);
        }
        // abstract class, skip.
        if (modifiers.contains(Modifier.ABSTRACT)) {
            throw new RouteException(String.format(
                    "The class %s is abstract. You can't annotate abstract classes with @%s.",
                    ((TypeElement) typeElement).getQualifiedName(), Route.class.getSimpleName()),
                    typeElement);
        }
    }

    private void error(Element element, String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    /**
     * Generate a config file to save all modules.
     */
    private void generateConfig(String allModules) {
        FieldSpec fieldSpec = FieldSpec.builder(String.class, Consts.ROUTER_CONFIG_FIELD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", allModules)
                .build();
        TypeSpec type = TypeSpec.classBuilder(Consts.ROUTER_CONFIG_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldSpec)
                .addJavadoc("Generated by Router. Do not edit it!\n")
                .build();
        try {
            JavaFile.builder(Consts.PACKAGE_NAME, type).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a .java file that contains route map.
     */
    private void generateCode(String clzName, Set<TypeElement> elements) {
        TypeElement activityType = elementUtils.getTypeElement("android.app.Activity");
        // Map<String, Class<? extends Activity>> map
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(activityType))));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "map").build();

        MethodSpec.Builder initActivityTable = MethodSpec.methodBuilder(Consts.ROUTE_TABLE_METHOD_NAME)
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

        TypeElement interfaceType = elementUtils.getTypeElement(Consts.ROUTE_TABLE_INTERFACE_NAME);
        TypeSpec type = TypeSpec.classBuilder(clzName)
                .addSuperinterface(ClassName.get(interfaceType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initActivityTable.build())
                .addJavadoc("Generated by Router. Do not edit it!\n")
                .build();
        try {
            JavaFile.builder(Consts.PACKAGE_NAME, type).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String combineClzName(String moduleName) {
        return capitalize(moduleName) + Consts.ROUTE_TABLE_CLASS_SUFFIX;
    }

    private String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
