package com.chenenyu.router.compiler.processor;

import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.compiler.util.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.chenenyu.router.compiler.util.Consts.ACTIVITY_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.CLASS_JAVA_DOC;
import static com.chenenyu.router.compiler.util.Consts.FRAGMENT_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.FRAGMENT_V4_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.HANDLE;
import static com.chenenyu.router.compiler.util.Consts.OPTION_MODULE_NAME;
import static com.chenenyu.router.compiler.util.Consts.PACKAGE_NAME;
import static com.chenenyu.router.compiler.util.Consts.ROUTE_ANNOTATION_TYPE;
import static com.chenenyu.router.compiler.util.Consts.ROUTE_TABLE;
import static com.chenenyu.router.compiler.util.Consts.ROUTE_TABLE_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.TABLE_INTERCEPTORS;

/**
 * {@link Route} annotation processor.
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
@SupportedAnnotationTypes(ROUTE_ANNOTATION_TYPE)
@SupportedOptions(OPTION_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RouteProcessor extends AbstractProcessor {
    private String mModuleName;
    private Logger mLogger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mModuleName = processingEnvironment.getOptions().get(OPTION_MODULE_NAME);
        mLogger = new Logger(processingEnvironment.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        mLogger.info(String.format(">>> %s: RouteProcessor begin... <<<", mModuleName));
        // 合法的TypeElement集合
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            if (element.getKind().isClass() && validateClass((TypeElement) element)) { // 注解在Class上的Route
                typeElements.add((TypeElement) element);
            } else if (element.getKind() == ElementKind.METHOD) { // 注解在Method上的Route
                validateMethod((ExecutableElement) element);
            }
        }
        if (mModuleName != null) {
            String validModuleName = mModuleName.replace(".", "_").replace("-", "_");
            generateRouteTable(validModuleName, typeElements);
            generateTargetInterceptors(validModuleName, typeElements);
        } else {
            throw new RuntimeException(String.format("No option `%s` passed to Route annotation processor.", OPTION_MODULE_NAME));
        }
        mLogger.info(String.format(">>> %s: RouteProcessor end. <<<", mModuleName));
        return true;
    }

    /**
     * Verify the annotated class. Must be a subtype of Activity or Fragment.
     */
    private boolean validateClass(TypeElement typeElement) {
        if (!isSubtype(typeElement, ACTIVITY_FULL_NAME) && !isSubtype(typeElement, FRAGMENT_V4_FULL_NAME)
                && !isSubtype(typeElement, FRAGMENT_FULL_NAME)) {
            mLogger.error(typeElement, String.format("%s is not a subclass of Activity or Fragment.",
                    typeElement.getSimpleName().toString()));
            return false;
        }
        Set<Modifier> modifiers = typeElement.getModifiers();
        // abstract class.
        if (modifiers.contains(Modifier.ABSTRACT)) {
            mLogger.error(typeElement, String.format("The class %s is abstract. You can't annotate abstract classes with @%s.",
                    (typeElement).getQualifiedName(), Route.class.getSimpleName()));
            return false;
        }
        return true;
    }

    private boolean isSubtype(Element typeElement, String type) {
        return processingEnv.getTypeUtils().isSubtype(typeElement.asType(),
                processingEnv.getElementUtils().getTypeElement(type).asType());
    }

    /**
     * Verify the annotated method.
     */
    private void validateMethod(ExecutableElement methodElement) {
        if (methodElement.getModifiers().contains(Modifier.ABSTRACT)) { // no abstract
            throw new RuntimeException(String.format("The method(%s) annotated by @Route is abstract.",
                    methodElement.getSimpleName()));
        }
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (!parameters.isEmpty()) { // params must be annotated by @InjectParam
            for (VariableElement variableElement : parameters) {
                // param's type must be String
                if (!variableElement.asType().toString().equals("java.lang.String")) {
                    throw new RuntimeException(String.format("The method's(%s) parameters must be String.",
                            methodElement.getSimpleName()));
                }
                InjectParam injectParam = variableElement.getAnnotation(InjectParam.class);
                if (injectParam == null) {
                    throw new RuntimeException(String.format("The method's(%s) parameters must be annotated by @InjectParam.",
                            methodElement.getSimpleName()));
                }
                if ("".equals(injectParam.key().trim())) {
                    throw new RuntimeException(String.format("The parameter(%s) annotated by @InjectParam must has a non-empty key.",
                            variableElement.getSimpleName()));
                }
            }
        }
    }

    /**
     * RouteTable.
     */
    private void generateRouteTable(String moduleName, Set<TypeElement> elements) {
        // Map<String, Class<?>> map
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(Object.class)));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "map").build();

        MethodSpec.Builder methodHandle = MethodSpec.methodBuilder(HANDLE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapParameterSpec);

        // 记录path->element，防止重复的route path
        Map<String, String> pathRecorder = new HashMap<>();

        for (TypeElement element : elements) {
            mLogger.info(String.format("Found routed target: %s", element.getQualifiedName()));
            Route route = element.getAnnotation(Route.class);
            String[] paths = route.value();
            for (String path : paths) {
                if (pathRecorder.containsKey(path)) {
                    throw new RuntimeException(String.format("Duplicate route path: %s[%s, %s]",
                            path, element.getQualifiedName(), pathRecorder.get(path)));
                }
                methodHandle.addStatement("map.put($S, $T.class)", path, ClassName.get(element));
                pathRecorder.put(path, element.getQualifiedName().toString());
            }
        }

        TypeElement interfaceType = processingEnv.getElementUtils().getTypeElement(ROUTE_TABLE_FULL_NAME);
        TypeSpec type = TypeSpec.classBuilder(capitalize(moduleName) + ROUTE_TABLE)
                .addSuperinterface(ClassName.get(interfaceType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodHandle.build())
                .addJavadoc(CLASS_JAVA_DOC)
                .build();
        try {
            JavaFile.builder(PACKAGE_NAME, type).build().writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TargetInterceptors.
     */
    private void generateTargetInterceptors(String moduleName, Set<TypeElement> elements) {
        // Map<Class<?>, String[]> map
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(Object.class)),
                TypeName.get(String[].class));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "map").build();
        MethodSpec.Builder methodHandle = MethodSpec.methodBuilder(HANDLE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapParameterSpec);
        boolean hasInterceptor = false; // flag
        for (TypeElement element : elements) {
            Route route = element.getAnnotation(Route.class);
            String[] interceptors = route.interceptors();
            if (interceptors.length > 1) {
                hasInterceptor = true;
                StringBuilder sb = new StringBuilder();
                for (String interceptor : interceptors) {
                    sb.append("\"").append(interceptor).append("\",");
                }
                methodHandle.addStatement("map.put($T.class, new String[]{$L})",
                        ClassName.get(element), sb.substring(0, sb.lastIndexOf(",")));
            } else if (interceptors.length == 1) {
                hasInterceptor = true;
                methodHandle.addStatement("map.put($T.class, new String[]{$S})",
                        ClassName.get(element), interceptors[0]);
            }
        }
        if (!hasInterceptor) { // if there are no interceptors, ignore.
            return;
        }
        TypeSpec type = TypeSpec.classBuilder(capitalize(moduleName) + TABLE_INTERCEPTORS)
                .addSuperinterface(ClassName.get(PACKAGE_NAME, TABLE_INTERCEPTORS))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodHandle.build())
                .addJavadoc(CLASS_JAVA_DOC)
                .build();
        try {
            JavaFile.builder(PACKAGE_NAME, type).build().writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
