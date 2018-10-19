package org.hjf.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.hjf.apt.annotation.InstanceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "org.hjf.apt.annotation.InstanceFactory",
})
public class InstanceFactoruProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final String CLASS_NAME = "InstanceFactory";

        // 1. 添加Class和其属性申明
        TypeSpec.Builder tb = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("@全局对象实例工厂 此类{@link $L}自动生成", InstanceFactoruProcessor.class.getName());

        // 2. 添加 <T> T create(String classPath) 方法
        tb.addMethod(MethodSpec.methodBuilder("create")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addException(InstantiationException.class)
                .addException(IllegalAccessException.class)
                .returns(TypeVariableName.get("<T> T"))
                .addCode(getBlock(roundEnvironment))
                .build());

        // 写入
        try {
            JavaFile javaFile = JavaFile.builder(RouterProcessor.PACKAGE_NAME, tb.build()).build();
            javaFile.writeTo(mFiler);
        } catch (IOException ignored) {
        }
        return true;
    }


    private CodeBlock getBlock(RoundEnvironment roundEnvironment) {
        CodeBlock.Builder blockBuilder = CodeBlock.builder();

        // switch
        blockBuilder.addStatement("String classPath = clazz.getName()");
        blockBuilder.beginControlFlow("switch (classPath)");

        // case ...
        ArrayList<ClassName> classNameCache = new ArrayList<>();
        for (TypeElement element : ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(InstanceFactory.class))) {
            // 标注 @InstanceFactory 不是class，报错
            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("Only classes can be annotated with @%s", InstanceFactory.class.getSimpleName()),
                        element);
                continue;
            }
            ClassName className = ClassName.get(element);
            if (classNameCache.contains(className)) {
                continue;
            }
            classNameCache.add(className);

            blockBuilder.addStatement("case $S: return (T) new $T() ", className.toString(), className);
        }

        // default
        blockBuilder.addStatement("default: return (T) clazz.newInstance()");
        blockBuilder.endControlFlow();
        return blockBuilder.build();
    }
}
