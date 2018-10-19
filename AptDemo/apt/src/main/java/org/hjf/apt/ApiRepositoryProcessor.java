package org.hjf.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;


import org.hjf.apt.annotation.ApiRepository;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import retrofit2.http.Path;

import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "org.hjf.apt.annotation.ApiRepository",
})
public class ApiRepositoryProcessor extends AbstractProcessor {

    private static final String IMPL_INSTANCE_NAME = "service";
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
        final String CLASS_NAME = "ApiRepository";

        // 1. 添加 ApiRepository.java 和其属性申明
        TypeSpec.Builder tb = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("@Api仓库 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                .addJavadoc("所有标记 {@link $T} 都会生成单例对象保存在此处\n", ApiRepository.class);

        // 2. 循环获取标记注解  @ApiRepository 的接口
        //    生成对应接口的实现类，并在ApiRepository中申明各实现类的对象
        //    标记注解 ApiRepository.class 的所有接口的实现类的 TypeSpec.Builder
        List<TypeSpec.Builder> implClassList = new ArrayList<>();
        List<ClassName> classPathCache = new ArrayList<>();
        for (TypeElement element : ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(ApiRepository.class))) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "正在处理" + element.toString());
            // 标注 @ApiRepository 不是 interface，报错
            if (element.getKind() != ElementKind.INTERFACE) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("Only classes can be annotated with @%s", ApiRepository.class.getSimpleName()),
                        element);
                continue;
            }
            ClassName className = ClassName.get(element);
            // 重复判断
            if (classPathCache.contains(className)) {
                continue;
            }
            classPathCache.add(className);

            // 4.1 获取实现类的代码
            implClassList.add(getImplementClassTypeSpecBuilder(element));

            // 4.1 申明变量字段： 实现类的对象
            ClassName implClassName = getInterfaceImplementClassName(className);
            tb.addField(FieldSpec.builder(implClassName, implClassName.simpleName())
                    .addJavadoc("@此字段 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                    .addJavadoc("是接口 {@link $L} 的实现类 {@link $L}的实例对象\n", className, implClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .build());
        }

        // 5. 初始化方法： init(Retrofit retrofit)
        // 初始化所有 @ApiRepository 接口的对象
        tb.addMethod(MethodSpec.methodBuilder("init")
                .addJavadoc("@方法 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("retrofit2", "Retrofit"), "retrofit")
                .addCode(getBlock4init(classPathCache))
                .build());

        // 6. 生成Java文件
        try {
            // 生成主类: ApiRepository.java
            JavaFile javaFile = JavaFile.builder(RouterProcessor.PACKAGE_NAME, tb.build()).build();
            javaFile.writeTo(mFiler);
            // 生成所有实现类
            for (TypeSpec.Builder builder : implClassList) {
                javaFile = JavaFile.builder(RouterProcessor.PACKAGE_NAME, builder.build()).build();
                javaFile.writeTo(mFiler);
            }
        } catch (IOException ignored) {
        }
        return true;
    }

    //  生成接口实现类
    private static TypeSpec.Builder getImplementClassTypeSpecBuilder(TypeElement interfaceElement) {
        ClassName className = ClassName.get(interfaceElement);
        ClassName implClassName = getInterfaceImplementClassName(className);
        // 1. 实现类声明
        TypeSpec.Builder tb = TypeSpec.classBuilder(implClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("@Api仓库 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                .addJavadoc("此类是接口 {@link $T} 接口的实现类\n", className);

        // 2. 申明变量字段： 实现类的对象
        tb.addField(FieldSpec.builder(className, IMPL_INSTANCE_NAME)
                .addJavadoc("@此字段 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                .addModifiers(Modifier.PRIVATE)
                .build());

        // 3. 实现类的构造方法
        tb.addMethod(MethodSpec.constructorBuilder()
                .addJavadoc("@构造方法 此类由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                .addParameter(className, "service")
                .addCode(CodeBlock.builder().addStatement("this." + IMPL_INSTANCE_NAME + " = service").build())
                .build());

        /*
         * 4. 遍历标注 @ApiRepository 的元素(类、方法、变量等)，之前已过滤非class标注
         *  -   1. @Path - 从 params 中找
         *  -   2. @
         */
        for (Element element : interfaceElement.getEnclosedElements()) {

            // 4.1 不是方法就跳过
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement methodElement = (ExecutableElement) element;
            // 4.2 返回值不是 retrofit2.Call 的方法，跳过
            TypeMirror returnType = methodElement.getReturnType();
            TypeName returnTypeName = TypeName.get(returnType);
            if (!returnTypeName.toString().startsWith("retrofit2.Call") &&
                    !returnTypeName.toString().equals("okhttp3.Call")) {
                continue;
            }

            // 4.3 申明方法和返回值
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addJavadoc("@此方法由 {@link $L} 自动生成\n", ApiRepositoryProcessor.class.getName())
                    .returns(returnTypeName)
                    .addModifiers(PUBLIC);

            // 4.4  获取方法所有注解，并处理
            //      支持多个注解
            //      TODO 处理 @Headers @POST @PUT @GET   实战时考虑
            // methodElement.getAnnotation(GET.class)

            // 4.5  遍历所有参数，并处理参数注解
            //      目前只支持参数有一个注解，按顺序排优先度
            boolean hasAddedParameter4HashMap = false;
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("return ")
                    .append(IMPL_INSTANCE_NAME).append(".")
                    .append(element.getSimpleName().toString()).append("(");
            for (VariableElement variableElement : methodElement.getParameters()) {
                // 处理 @Path
                Path path = variableElement.getAnnotation(Path.class);
                if (path != null) {
                    // 添加通用参数  HashMap<String,String>
                    if (!hasAddedParameter4HashMap) {
                        hasAddedParameter4HashMap = true;
                        methodBuilder.addParameter(ParameterizedTypeName
                                .get(HashMap.class, String.class, String.class), "params");
                    }
                    // variableElement.getSimpleName()  参数名
                    // variableElement.asType() 参数类型
                    // path.value() @Path("value") 中的value
                    ApiRepositoryProcessor.getString4BaseData(stringBuilder, variableElement, path);
                    continue;
                }
                // TODO 处理 @Query @Body 实战时考虑

                // other : no annotation or other annotation
                methodBuilder.addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString());
                stringBuilder.append(variableElement.getSimpleName().toString()).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length() - 2); // 去掉最后的 ', ' 符号
            stringBuilder.append(")");
            // 4.6 调用 retrofit2.create(service) 生成的对象相应方法
            methodBuilder.addCode(CodeBlock.builder().addStatement(stringBuilder.toString()).build());

            // 添加方法
            tb.addMethod(methodBuilder.build());
        }
        return tb;
    }

    // get init method block
    private static CodeBlock getBlock4init(List<ClassName> classPathCache) {
        CodeBlock.Builder blockBuilder = CodeBlock.builder();
        for (ClassName className : classPathCache) {
            ClassName implClassName = getInterfaceImplementClassName(className);
            blockBuilder.addStatement("$T = new $T(retrofit.create($T.class))", implClassName, implClassName, className);
        }
        return blockBuilder.build();
    }

    // get class name for interface with annotation @ApiRepository
    private static ClassName getInterfaceImplementClassName(ClassName className) {
        return ClassName.get(RouterProcessor.PACKAGE_NAME, className.simpleName() + "Impl");
    }

    // string -> BaseData
    private static void getString4BaseData(final StringBuilder stringBuilder, VariableElement
            variableElement, Path path) {
        // long
        if (variableElement.asType().getKind() == TypeKind.LONG) {
            stringBuilder.append("Long.parseLong(");
        }
        // int
        else if (variableElement.asType().getKind() == TypeKind.INT) {
            stringBuilder.append("Integer.parseInt(");
        }
        // short
        else if (variableElement.asType().getKind() == TypeKind.SHORT) {
            stringBuilder.append("Short.parseShort(");
        }
        // string
        else {
            stringBuilder.append("(");
        }
        stringBuilder.append("params.get(\"").append(path.value()).append("\")");
        stringBuilder.append("), ");
    }
}
