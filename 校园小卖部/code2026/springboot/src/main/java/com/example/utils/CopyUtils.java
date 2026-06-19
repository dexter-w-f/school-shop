package com.example.utils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import java.io.File;

public class CopyUtils {
    // 获取项目根目录（根据实际情况调整）
    private static String projectRoot = System.getProperty("user.dir") + "/校园小卖部/code2026";
    private static String packagePath = projectRoot + "/springboot/src/main/java/com/example";
    private static String resourcesPath = projectRoot + "/springboot/src/main/resources";
    private static String vuePath = projectRoot + "/vue/src/views/manager";

    private static String controllerSuffix = "Controller.java";
    private static String serviceSuffix = "Service.java";
    private static String mapperSuffix = "Mapper.java";
    private static String mapperXmlSuffix = "Mapper.xml";
    private static String vueSuffix = ".vue";

    // 拷贝代码小工具
    public static void main(String[] args) {
        // 复制源文件前缀
        String sourceName = "Category";
        // 复制后的目标文件前缀
        String targetName = "Collect";

        // 复制一整套的 controller、Service、Mapper、Mapper.xml
        copyMapperXml(sourceName, targetName);
        copyMapper(sourceName, targetName);
        copyService(sourceName, targetName);
        copyController(sourceName, targetName);

        // 复制Vue文件
        copyVue(sourceName, targetName);
    }

    // 复制Mapper.xml
    public static void copyMapperXml(String sourceName, String targetName) {
        // mapper的路径前缀
        String mapperXmlPathPrefix = resourcesPath + "/mapper/";
        String sourceFilePath = mapperXmlPathPrefix + sourceName + mapperXmlSuffix;

        // 检查源文件是否存在
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            System.err.println("错误：源文件不存在 - " + sourceFilePath);
            System.err.println("当前工作目录：" + System.getProperty("user.dir"));
            return;
        }

        // 读取文件的内容
        String content = FileUtil.readUtf8String(sourceFilePath);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Mapepr.xml
        File targetFile = FileUtil.touch(mapperXmlPathPrefix + targetName + mapperXmlSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + mapperXmlSuffix + "复制成功！");
    }


    // 复制Mapper
    public static void copyMapper(String sourceName, String targetName) {
        // mapper的路径前缀
        String mapperPathPrefix = packagePath + "/mapper/";
        String sourceFilePath = mapperPathPrefix + sourceName + mapperSuffix;

        // 检查源文件是否存在
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            System.err.println("错误：源文件不存在 - " + sourceFilePath);
            System.err.println("当前工作目录：" + System.getProperty("user.dir"));
            return;
        }

        // 读取文件的内容
        String content = FileUtil.readUtf8String(sourceFilePath);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Mapepr
        File targetFile = FileUtil.touch(mapperPathPrefix + targetName + mapperSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + mapperSuffix + "复制成功！");
    }

    // 复制Service
    public static void copyService(String sourceName, String targetName) {
        // service的路径前缀
        String servicePathPrefix = packagePath + "/service/";
        String sourceFilePath = servicePathPrefix + sourceName + serviceSuffix;

        // 检查源文件是否存在
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            System.err.println("错误：源文件不存在 - " + sourceFilePath);
            System.err.println("当前工作目录：" + System.getProperty("user.dir"));
            return;
        }

        // 读取文件的内容
        String content = FileUtil.readUtf8String(sourceFilePath);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Service
        File targetFile = FileUtil.touch(servicePathPrefix + targetName + serviceSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + serviceSuffix + "复制成功！");
    }

    // 复制Controller
    public static void copyController(String sourceName, String targetName) {
        // controller的路径前缀
        String controllerPathPrefix = packagePath + "/controller/";
        String sourceFilePath = controllerPathPrefix + sourceName + controllerSuffix;

        // 检查源文件是否存在
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            System.err.println("错误：源文件不存在 - " + sourceFilePath);
            System.err.println("当前工作目录：" + System.getProperty("user.dir"));
            return;
        }

        // 读取文件的内容
        String content = FileUtil.readUtf8String(sourceFilePath);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Controller
        File targetFile = FileUtil.touch(controllerPathPrefix + targetName + controllerSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + controllerSuffix + "复制成功！");
    }

    // 复制Vue
    public static void copyVue(String sourceName, String targetName) {
        String sourceFilePath = vuePath + "/" + sourceName + vueSuffix;

        // 检查源文件是否存在
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            System.err.println("错误：源文件不存在 - " + sourceFilePath);
            System.err.println("当前工作目录：" + System.getProperty("user.dir"));
            return;
        }

        // 读取文件的内容
        String content = FileUtil.readUtf8String(sourceFilePath);
        // 替换文件的内容
        String result = StrUtil.replace(content, "/" + StrUtil.lowerFirst(sourceName),  "/" + StrUtil.lowerFirst(targetName));// 替换小写的后端接口路径

        // 创建目标的Vue
        File targetFile = FileUtil.touch(vuePath + "/" + targetName + vueSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + vueSuffix + "复制成功！");
    }

}