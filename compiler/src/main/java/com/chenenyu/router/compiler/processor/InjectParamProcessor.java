package com.chenenyu.router.compiler.processor;

import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.compiler.util.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static com.chenenyu.router.compiler.util.Consts.ACTIVITY_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.CLASS_JAVA_DOC;
import static com.chenenyu.router.compiler.util.Consts.FRAGMENT_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.FRAGMENT_V4_FULL_NAME;
import static com.chenenyu.router.compiler.util.Consts.INNER_CLASS_NAME;
import static com.chenenyu.router.compiler.util.Consts.METHOD_INJECT;
import static com.chenenyu.router.compiler.util.Consts.OPTION_MODULE_NAME;
import static com.chenenyu.router.compiler.util.Consts.PACKAGE_NAME;
import static com.chenenyu.router.compiler.util.Consts.PARAM_ANNOTATION_TYPE;

/**
 * {@link InjectParam} annotation processor.
 * <p>
 * Created by chenenyu on 2017/6/12.
 */
@SupportedAnnotationTypes(PARAM_ANNOTATION_TYPE)
@SupportedOptions(OPTION_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InjectParamProcessor extends AbstractProcessor {
    private String mModuleName;
    private Logger mLogger;
    private Map<TypeElement, List<Element>> mClzAndParams = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mModuleName = processingEnvironment.getOptions().get(OPTION_MODULE_NAME);
        mLogger = new Logger(processingEnvironment.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(InjectParam.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        mLogger.info(String.format(">>> %s: InjectParamProcessor begin... <<<", mModuleName));
        parseParams(elements);
        try {
            generate();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            mLogger.error("Exception occurred when generating class file.");
            e.printStackTrace();
        }
        mLogger.info(String.format(">>> %s: InjectParamProcessor end. <<<", mModuleName));
        return true;
    }

    private void parseParams(Set<? extends Element> elements) {
        for (Element element : elements) {
            if (element.getKind().isField()) { // 注解到字段，支持Activity/Fragment
                TypeElement typeElement = (TypeElement) element.getEnclosingElement();

                if (mClzAndParams.containsKey(typeElement)) {
                    mClzAndParams.get(typeElement).add(element);
                } else {
                    List<Element> params = new ArrayList<>();
                    params.add(element);
                    mClzAndParams.put(typeElement, params);
                }
            }
        }
    }

    private void generate() throws IllegalAccessException, IOException {
        final String TARGET = "target";
        final String EXTRAS = "extras";
        final String OBJ = "obj";

        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, OBJ).build();

        for (Map.Entry<TypeElement, List<Element>> entry : mClzAndParams.entrySet()) {
            TypeElement parent = entry.getKey();
            List<Element> params = entry.getValue();

            String qualifiedName = parent.getQualifiedName().toString();
            String simpleName = parent.getSimpleName().toString();
            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
            String fileName = simpleName + INNER_CLASS_NAME;

            // validate
            boolean isActivity;
            if (isSubtype(parent, ACTIVITY_FULL_NAME)) {
                isActivity = true;
            } else if (isSubtype(parent, FRAGMENT_V4_FULL_NAME) || isSubtype(parent, FRAGMENT_FULL_NAME)) {
                isActivity = false;
            } else {
                throw new IllegalAccessException(
                        String.format("The target class %s must be Activity or Fragment.", simpleName));
            }

            mLogger.info(String.format("Start to process injected params in %s ...", simpleName));

            // @Override
            // public void inject(Object obj) {}
            MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(objectParamSpec);

            // XXXActivity target = (XXXActivity) obj;
            injectMethodBuilder.addStatement("$T $L = ($T) $L",
                    ClassName.get(parent), TARGET, ClassName.get(parent), OBJ);
            if (isActivity) { // Bundle extras = target.getIntent().getExtras();
                injectMethodBuilder.addStatement("$T $L = $L.getIntent().getExtras()",
                        ClassName.get("android.os", "Bundle"), EXTRAS, TARGET);
            } else { // Bundle extras = target.getArguments();
                injectMethodBuilder.addStatement("$T $L = $L.getArguments()",
                        ClassName.get("android.os", "Bundle"), EXTRAS, TARGET);
            }

            for (Element param : params) {
                InjectParam injectParam = param.getAnnotation(InjectParam.class);
                String fieldName = param.getSimpleName().toString();
                String key = isEmpty(injectParam.key()) ? fieldName : injectParam.key();

                StringBuilder statement = new StringBuilder();
                if (param.getModifiers().contains(Modifier.PRIVATE)) {
                    mLogger.warn(param, String.format(
                            "Found private field: %s, please remove 'private' modifier for a better performance.", fieldName));

                    String reflectName = "field_" + fieldName;

                    injectMethodBuilder.beginControlFlow("try")
                            .addStatement("$T $L = $T.class.getDeclaredField($S)",
                                    ClassName.get(Field.class), reflectName, ClassName.get(parent), fieldName)
                            .addStatement("$L.setAccessible(true)", reflectName);

                    Object[] args;
                    // field_xxx.set(target, extras.getXXX("key"
                    statement.append("$L.set($L, $L.get")
                            .append(getAccessorType(param.asType()))
                            .append("(")
                            .append("$S");
                    // , (FieldType) field_xxx.get(target)
                    if (supportDefaultValue(param.asType())) {
                        statement.append(", ($T) $L.get($L)");
                        args = new Object[]{reflectName, TARGET, EXTRAS, key,
                                ClassName.get(param.asType()), reflectName, TARGET};
                    } else {
                        args = new Object[]{reflectName, TARGET, EXTRAS, key};
                    }
                    // ))
                    statement.append("))");

                    injectMethodBuilder.addStatement(statement.toString(), args)
                            .nextControlFlow("catch ($T e)", Exception.class)
                            .addStatement("e.printStackTrace()")
                            .endControlFlow();
                } else {
                    Object[] args;
                    // target.field = (FieldType) extras.getXXX("key"

                    statement.append("$L.$L = ($T) $L.get")
                            .append(getAccessorType(param.asType())).append("(")
                            .append("$S");
                    // , target.field
                    if (supportDefaultValue(param.asType())) {
                        statement.append(", $L.$L");
                        args = new Object[]{TARGET, fieldName, ClassName.get(param.asType()), EXTRAS, key, TARGET, fieldName};
                    } else {
                        args = new Object[]{TARGET, fieldName, ClassName.get(param.asType()), EXTRAS, key};
                    }
                    statement.append(")");

                    injectMethodBuilder.addStatement(statement.toString(), args);
                }
            }

            TypeSpec typeSpec = TypeSpec.classBuilder(fileName)
                    .addJavadoc(CLASS_JAVA_DOC)
                    .addSuperinterface(ClassName.get(PACKAGE_NAME, "ParamInjector"))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(injectMethodBuilder.build())
                    .build();

            JavaFile.builder(packageName, typeSpec).build().writeTo(processingEnv.getFiler());

            mLogger.info(String.format("Params in class %s have been processed: %s.", simpleName, fileName));
        }
    }

    private boolean supportDefaultValue(TypeMirror typeMirror) {
        if (typeMirror instanceof PrimitiveType) {
            return true;
        }
        if (isSubtype(typeMirror, "java.lang.String") || isSubtype(typeMirror, "java.lang.CharSequence")) {
            return true;
        }
        return false;
    }

    /**
     * Computes the string to append to 'get' or 'set' to get a valid Bundle method name.
     * For example, for the type int[], will return 'IntArray', which leads to the methods 'putIntArray' and 'getIntArray'
     *
     * @param typeMirror The type to access in the bundle
     * @return The string to append to 'get' or 'put'
     */
    private String getAccessorType(TypeMirror typeMirror) {
        if (typeMirror instanceof PrimitiveType) {
            return typeMirror.toString().toUpperCase().charAt(0) + typeMirror.toString().substring(1);
        } else if (typeMirror instanceof DeclaredType) {
            Element element = ((DeclaredType) typeMirror).asElement();
            if (element instanceof TypeElement) {
                if (isSubtype(element, "java.util.List")) { // ArrayList
                    List<? extends TypeMirror> typeArgs = ((DeclaredType) typeMirror).getTypeArguments();
                    if (typeArgs != null && !typeArgs.isEmpty()) {
                        TypeMirror argType = typeArgs.get(0);
                        if (isSubtype(argType, "java.lang.Integer")) {
                            return "IntegerArrayList";
                        } else if (isSubtype(argType, "java.lang.String")) {
                            return "StringArrayList";
                        } else if (isSubtype(argType, "java.lang.CharSequence")) {
                            return "CharSequenceArrayList";
                        } else if (isSubtype(argType, "android.os.Parcelable")) {
                            return "ParcelableArrayList";
                        }
                    }
                } else if (isSubtype(element, "android.os.Bundle")) {
                    return "Bundle";
                } else if (isSubtype(element, "java.lang.String")) {
                    return "String";
                } else if (isSubtype(element, "java.lang.CharSequence")) {
                    return "CharSequence";
                } else if (isSubtype(element, "android.util.SparseArray")) {
                    return "SparseParcelableArray";
                } else if (isSubtype(element, "android.os.Parcelable")) {
                    return "Parcelable";
                } else if (isSubtype(element, "java.io.Serializable")) {
                    return "Serializable";
                } else if (isSubtype(element, "android.os.IBinder")) {
                    return "Binder";
                }
            }
        } else if (typeMirror instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) typeMirror;
            TypeMirror compType = arrayType.getComponentType();
            if (compType instanceof PrimitiveType) {
                return compType.toString().toUpperCase().charAt(0) + compType.toString().substring(1) + "Array";
            } else if (compType instanceof DeclaredType) {
                Element compElement = ((DeclaredType) compType).asElement();
                if (compElement instanceof TypeElement) {
                    if (isSubtype(compElement, "java.lang.String")) {
                        return "StringArray";
                    } else if (isSubtype(compElement, "java.lang.CharSequence")) {
                        return "CharSequenceArray";
                    } else if (isSubtype(compElement, "android.os.Parcelable")) {
                        return "ParcelableArray";
                    }
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isSubtype(Element typeElement, String type) {
        return processingEnv.getTypeUtils().isSubtype(typeElement.asType(),
                processingEnv.getElementUtils().getTypeElement(type).asType());
    }

    private boolean isSubtype(TypeMirror typeMirror, String type) {
        return processingEnv.getTypeUtils().isSubtype(typeMirror,
                processingEnv.getElementUtils().getTypeElement(type).asType());
    }

    private boolean isEmpty(CharSequence c) {
        return c == null || c.length() == 0;
    }

}
