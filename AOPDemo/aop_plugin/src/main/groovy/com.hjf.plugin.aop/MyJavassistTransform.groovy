package com.hjf.plugin.aop

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import javassist.ClassPool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class MyJavassistTransform extends Transform {
    Project project

    public MyJavassistTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {// 设置我们自定义的Transform对应的Task名称
        return "MyJavassistTrans"
    }


    @Override
    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型这样确保其他类型的文件不会传入
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }


    @Override
    // 指定Transform的作用范围
    Set<QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT, QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS, QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        System.out.println("javassist init transform start")

        // Transform 的 inputs 有两种类型，一种是目录，一种是jar包，要分开遍历
        transformInvocation.inputs.each { TransformInput input ->

            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->

                // 自定义修改任务
                MyInject.injectDir(jarInput.file.getAbsolutePath(), project)

                // jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name.replace(".jar", "")
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                //生成输出路径
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }

            // 对类型为 目录(文件夹) 的input进行遍历, 开始自动注入
            input.directoryInputs.each { DirectoryInput directoryInput ->

                // 自定义修改任务
                MyInject.injectDir(directoryInput.file.absolutePath, project)

                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
        ClassPool.getDefault().clearImportedPackages()
        System.out.println("javassist init transform start")
    }
}