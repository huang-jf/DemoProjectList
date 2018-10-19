package org.hjf.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.hjf.apt.annotation.Extra;
import org.hjf.apt.annotation.Router;
import org.hjf.apt.annotation.SceneTransition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/*
 * $L: 直接使用 android.support.v4.app.ActivityCompat.startActivity()
 * $T: 导入后再使用 import android.support.v4.app.ActivityCompat;  ActivityCompat.startActivity();
 * $S: 字符串，对于丢如内容强制加上引用符号， "content"
 */
/*
 * Element 介绍
 *
 * package com.example; // PackageElement
 *
 * public class Foo { // TypeElement private
 *
 *      int a; // VariableElement
 *
 *      private Foo other; // VariableElement
 *
 *      public Foo () {} // ExecuteableElement
 *
 *      public void setA ( // ExecuteableElement
 *          int newA // TypeElement ) {
 *      }
 * }
 */
/* 自动生成 javax.annotation.processing.IProcessor 文件 */
@AutoService(Processor.class)
/* java版本支持 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
/*
 * 将指定注解 注册 到 此注解处理器 上
 * 若没有注册注解或是注册的注解没被使用，不会生成Java文件
 */
@SupportedAnnotationTypes({
        "org.hjf.apt.annotation.Router",
})
public class RouterProcessor extends AbstractProcessor {
    public static final String PACKAGE_NAME = "com.hjf";

    /**
     * 文件相关的辅助类
     */
    public Filer mFiler;
    /**
     * 元素相关的辅助类
     */
    private Elements mElements;
    /**
     * 日志相关的辅助类
     */
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // 1. 添加Class和其属性申明
        TypeSpec.Builder tb = TypeSpec.classBuilder("TRouter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("@全局路由器 此类由{@link $L}自动生成", RouterProcessor.class.getName());

        // 2. 增加静态变量
        FieldSpec extraField = FieldSpec.builder(ParameterizedTypeName.get(HashMap.class, String.class, Object.class),
                "mCurActivityExtra")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();
        tb.addField(extraField);

        // 3.1 add method: go(String activityClassPath, HashMap extra, View view) 方法
        tb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "activityClassPath")
                .addParameter(HashMap.class, "extra")
                // android class
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addCode(getBlock4goString(roundEnv))
                .build());

        // 3.2 add method: go(Class clazz, HashMap extra, View view) 方法
        tb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addParameter(HashMap.class, "extra")
                // android class
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addCode(getBlock4goClass())
                .build());

        // 3.3 add overloading method: go(Class clazz, View view)
        tb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addCode("go(clazz,null,view);\n")
                .build());

        // 3.4 add overloading method: go(Class clazz)
        tb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Class.class, "clazz")
                .addCode("go(clazz,null,null);\n")
                .build());

        // 3.4 add method: bind(Activity activity)
        tb.addMethod(MethodSpec.methodBuilder("bind")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get("android.app", "Activity"), "context")
                .addCode(getBlock4bind(roundEnv))
                .build());

        // 3.5 获取帮助类对象方法
        ClassName helperClassName = ClassName.get("com.hjf", "ExtraHelper");
        tb.addMethod(MethodSpec.methodBuilder("addExtra")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(PUBLIC, STATIC)
                .returns(helperClassName)
                .addParameter(String.class, "key")
                .addParameter(Object.class, "value")
                .addCode("$T helper = new $T();\nhelper.addExtra(key, value);\nreturn helper;", helperClassName, helperClassName)
                .build());

        // 4.1 帮助类 - ExtraHelper
        ClassName routerClassName = ClassName.get("com.hjf", "TRouter");
        TypeSpec.Builder ehb = TypeSpec.classBuilder("ExtraHelper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("@全局路由器参数传递帮助类 此类由{@link $L}自动生成", RouterProcessor.class.getName());
        FieldSpec helperExtraField = FieldSpec.builder(ParameterizedTypeName.get(HashMap.class, String.class, Object.class),
                "extra")
                .addModifiers(Modifier.PRIVATE)
                .build();
        ehb.addField(helperExtraField);
        // 4.2 添加参数方法
        ehb.addMethod(MethodSpec.methodBuilder("addExtra")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(Object.class, "value")
                .returns(helperClassName)
                .addCode("if(extra==null){\nextra = new HashMap<>();\n}\nextra.put(key,value);\nreturn this;\n", helperClassName)
                .build());
        // 4.2 ExtraHelper.go(Class clazz, View view) 方法
        ehb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(PUBLIC)
                .addParameter(Class.class, "clazz")
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addCode("$T.go(clazz, extra, view);", routerClassName)
                .build());
        // 4.3 ExtraHelper.go(Class clazz) 方法
        ehb.addMethod(MethodSpec.methodBuilder("go")
                .addJavadoc("@此方法由apt自动生成")
                .addModifiers(PUBLIC)
                .addParameter(Class.class, "clazz")
                .addCode("$T.go(clazz, extra, null);", routerClassName)
                .build());

        try {
            // 5.1 指定 TRouter 包名，生成源代码
            JavaFile javaFile = JavaFile.builder("com.hjf", tb.build()).build();
            // 5.2 在 app module/build/generated/source/apt/包名目录下，生成TRouter.java文件
            javaFile.writeTo(mFiler);
            // 5.3 生成帮助类
            javaFile = JavaFile.builder("com.hjf", ehb.build()).build();
            javaFile.writeTo(mFiler);

        } catch (IOException ignored) {
        }
        return true;
    }

    // for method go(String activityClassPath, HashMap extra, View view)
    private CodeBlock getBlock4goString(RoundEnvironment roundEnv) {

        // 导入需要用到的 android 专属类（Java Lib 不能直接引入）
        ClassName mIntentClassName = ClassName.get("android.content", "Intent");
        ClassName mActivityCompatName = ClassName.get("android.support.v4.app", "ActivityCompat");
        ClassName mActivityOptionsCompatName = ClassName.get("android.support.v4.app", "ActivityOptionsCompat");
        ClassName appClassName = ClassName.get("com.hjf", "MyApp");
        ClassName mActivityName = ClassName.get("android.app", "Activity");

        // 组装代码块，用于方法内容添加
        // $L: 直接使用 android.support.v4.app.ActivityCompat.startActivity()
        // $T: 导入后再使用 import android.support.v4.app.ActivityCompat;  ActivityCompat.startActivity();
        // $S: 字符串，对于丢如内容强制加上引用符号， "content"
        CodeBlock.Builder blockBuilderGo = CodeBlock.builder();
        blockBuilderGo.addStatement("mCurActivityExtra=extra");
        blockBuilderGo.addStatement("$T context = $T.getTopActivity()", mActivityName, appClassName);
        blockBuilderGo.beginControlFlow(" switch (activityClassPath)");// {}括号开始

        // 1. 遍历被注释的类，有除重处理
        List<ClassName> classPathCache = new ArrayList<>();
        // 1.1. 遍历标注 @ApiRepository 的元素(类、方法、变量等)，筛选出class标注
        for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(Router.class))) {
            // 标注 @Router 不是class，报错
            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("Only classes can be annotated with @%s", Router.class.getSimpleName()),
                        element);
                continue;
            }
            ClassName className = ClassName.get(element);
            // 重复判断
            if (classPathCache.contains(className)) {
                continue;
            }
            classPathCache.add(className);

            blockBuilderGo.add("case $S: \n", className.toString());//1

            // 检查是否有转场动画，字段标注
            Element sceneTransitionElement = null;
            String sceneTransitionName = null;
            // 遍历标注 @ApiRepository 的元素(类、方法、变量等)，之前已过滤非class标注
            for (Element childElement : element.getEnclosedElements()) {
                SceneTransition mSceneTransitionAnnotation = childElement.getAnnotation(SceneTransition.class);
                // 没有标注 @SceneTransition 的字段，跳过
                if (mSceneTransitionAnnotation == null) {
                    continue;
                }
                // 在标注 @Router 的class中，标注 @SceneTransition 不是字段的话，报错
                if (childElement.getKind() != ElementKind.FIELD) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR,
                            String.format("Only field can be annotated with @%s", Router.class.getSimpleName()),
                            element);
                    continue;
                }
                sceneTransitionElement = childElement;
                sceneTransitionName = mSceneTransitionAnnotation.value();
            }

            // 有转场动画
            if (sceneTransitionElement != null) {
                blockBuilderGo.add("$L.startActivity(context," +//2
                                "\nnew $L(context," +//3
                                "\n$L.class)," +//4
                                "\n$T.makeSceneTransitionAnimation(" +//5
                                "\ncontext,view," +//6
                                "\n$S).toBundle());", //7
                        mActivityCompatName,//2
                        mIntentClassName,//3
                        element,//4
                        mActivityOptionsCompatName,//5
                        sceneTransitionName);//6
            }
            // 默认最简单打开
            else {
                blockBuilderGo.add("context.startActivity(" +//2
                                "\nnew $L(context," +//3
                                "\n$L.class));", //7
                        mIntentClassName,//3
                        element //4
                );
            }
            blockBuilderGo.addStatement("\nbreak");//1
        }
        blockBuilderGo.addStatement("default: break");
        blockBuilderGo.endControlFlow();
        return blockBuilderGo.build();
    }

    // for method go(Class clazz, HashMap extra, View view)
    private CodeBlock getBlock4goClass() {
        // 利用 clazz 对象获取 ActivityClassPath
        CodeBlock.Builder blockBuilderGo = CodeBlock.builder();
        blockBuilderGo.addStatement("String activityClassPath = clazz.getName()");
        blockBuilderGo.addStatement("go(activityClassPath, extra, view)");
        return blockBuilderGo.build();
    }


    private CodeBlock getBlock4bind(RoundEnvironment roundEnv) {
        CodeBlock.Builder blockBuilderBind = CodeBlock.builder();

        blockBuilderBind.addStatement("if(mCurActivityExtra==null) return");
        blockBuilderBind.beginControlFlow(" switch (context.getClass().getName())");//括号开始

        // 1. 遍历被注释的类，有除重处理
        List<String> classNameCache = new ArrayList<>();
        for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(Router.class))) {
            // 标注 @Router 不是class，跳过
            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("Only classes can be annotated with @%s", Router.class.getSimpleName()),
                        element);
                continue;
            }
            String className = ClassName.get(element).toString();
            // 重复判断
            if (classNameCache.contains(className)) {
                continue;
            }
            classNameCache.add(className);

            // 检查是否有传递参数
            List<Element> mExtraElements = new ArrayList<>();
            List<String> mExtraElementKeys = new ArrayList<>();
            // 遍历标注 @ApiRepository 的元素(类、方法、变量等)，之前已过滤非class标注
            for (Element childElement : element.getEnclosedElements()) {
                Extra mExtraAnnotation = childElement.getAnnotation(Extra.class);
                // 没有标注 @Extra 的字段，跳过
                if (mExtraAnnotation == null) {
                    continue;
                }
                // 在标注 @Router 的class中，标注 @Extra 不是字段的话，跳过
                if (childElement.getKind() != ElementKind.FIELD) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR,
                            String.format("Only field can be annotated with @%s", Router.class.getSimpleName()),
                            element);
                    continue;
                }
                mExtraElementKeys.add(mExtraAnnotation.value());
                mExtraElements.add(childElement);
            }

            blockBuilderBind.add("case $S: \n", className);//1
            // 获取传递参数
            for (int i = 0; i < mExtraElements.size(); i++) {
                Element mFiled = mExtraElements.get(i);
                blockBuilderBind.add("(($T)context)." +//1
                                "$L" +//2
                                "= ($T) " +//3
                                "mCurActivityExtra.get(" +//4
                                "$S);\n",//5
                        element,//1
                        mFiled,//2
                        mFiled,//3
                        mExtraElementKeys.get(i)//5
                );//5
            }
            blockBuilderBind.addStatement("\nbreak");//1
        }
        blockBuilderBind.addStatement("default: break");
        blockBuilderBind.endControlFlow();
        return blockBuilderBind.build();
    }
}
