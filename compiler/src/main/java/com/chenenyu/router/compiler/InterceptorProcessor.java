package com.chenenyu.router.compiler;

import com.chenenyu.router.annotation.Interceptor;
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
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.chenenyu.router.compiler.Consts.CLASS_JAVA_DOC;
import static com.chenenyu.router.compiler.Consts.INTERCEPTORS;
import static com.chenenyu.router.compiler.Consts.INTERCEPTORS_METHOD_NAME;
import static com.chenenyu.router.compiler.Consts.INTERCEPTOR_ANNOTATION_TYPE;
import static com.chenenyu.router.compiler.Consts.INTERCEPTOR_INTERFACE;
import static com.chenenyu.router.compiler.Consts.OPTION_MODULE_NAME;
import static com.chenenyu.router.compiler.Consts.PACKAGE_NAME;

/**
 * {@link Interceptor} annotation processor.
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@SupportedAnnotationTypes(INTERCEPTOR_ANNOTATION_TYPE)
@SupportedOptions(OPTION_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InterceptorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Interceptor.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        // 合法的TypeElement集合
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            if (validateElement(element)) {
                typeElements.add((TypeElement) element);
            } else {
                error(element, "The annotated element is not a implementation class of %s",
                        INTERCEPTOR_INTERFACE);
            }
        }

        String moduleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
        if (moduleName != null) {
            generateInterceptors(moduleName, typeElements);
        } else {
            error(null, "No option `%s` passed to annotation processor.", OPTION_MODULE_NAME);
        }
        return true;
    }

    private void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    private boolean validateElement(Element element) {
        return element.getKind().isClass() && processingEnv.getTypeUtils().isAssignable(element.asType(),
                processingEnv.getElementUtils().getTypeElement(INTERCEPTOR_INTERFACE).asType());
    }

    private void generateInterceptors(String moduleName, Set<TypeElement> elements) {
        /*
         * params
         */
        TypeElement interceptorType = processingEnv.getElementUtils().getTypeElement(INTERCEPTOR_INTERFACE);
        // Map<String, Class<? extends RouteInterceptor>> map
        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(interceptorType))));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "map").build();
        /*
         * method
         */
        MethodSpec.Builder handleInterceptors = MethodSpec.methodBuilder(INTERCEPTORS_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(mapParameterSpec);
        for (TypeElement element : elements) {
            Interceptor interceptor = element.getAnnotation(Interceptor.class);
            String name = interceptor.value();
            handleInterceptors.addStatement("map.put($S, $T.class)", name, ClassName.get(element));
        }

        /*
         * class
         */
        TypeSpec type = TypeSpec.classBuilder(capitalize(moduleName) + INTERCEPTORS)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(handleInterceptors.build())
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
