package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import com.example.common.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件相关操作接口
 */
@RestController
@RequestMapping("/files")
public class FileController {

    // 文件存储目录（与 WebConfig 的静态资源映射保持一致）
    private static final String FILE_DIR = com.example.utils.FileStorage.baseDir();

    // 允许上传的文件类型
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg",
        "mp4", "avi", "mov", "wmv",
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf",
        "txt"
    );

    @Value("${fileBaseUrl}")
    private String fileBaseUrl;

    @Value("${server.port}")
    private String port;

    /**
     * 文件上传（仅管理员）
     */
    @PostMapping("/upload")
    public Result upload(HttpServletRequest request, MultipartFile file) {
        try {
            com.example.utils.AdminControllerUtils.requireAdmin(request);
        } catch (RuntimeException e) {
            return Result.error("无权限上传文件");
        }
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error("文件名为空");
        }
        String ext = getExt(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error("不支持的文件类型");
        }
        String safeName = safeFileName(originalFilename);
        // 定义文件的唯一标识
        String fileName = java.util.UUID.randomUUID() + (ext.isBlank() ? "" : ("." + ext));
        // 拼接完整的文件存储路径
        String realFilePath = FILE_DIR + fileName;
        try {
            if (!FileUtil.isDirectory(FILE_DIR)) {
                FileUtil.mkdir(FILE_DIR);
            }
            FileUtil.writeBytes(file.getBytes(), realFilePath);
        } catch (IOException e) {
            return Result.error("文件上传失败");
        }

        // 返回文件下载的地址
        String url = fileBaseUrl + ":" + port + "/files/download/" + fileName;
        return Result.success(url);
    }

    private void doDownload(String fileName, HttpServletResponse response) {
        // 说明：文件的公开读取已交给 WebConfig 的静态资源映射（/files/download/**），
        // 因为浏览器的 <img> 无法携带 token 请求头。此处保留工具方法仅用于内部导出场景。
        java.nio.file.Path base;
        java.nio.file.Path target;
        try {
            base = java.nio.file.Paths.get(FILE_DIR).toAbsolutePath().normalize();
            target = base.resolve(fileName).normalize();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!target.startsWith(base) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // 设置下载文件http响应头
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        try {
            byte[] bytes = FileUtil.readBytes(target.toFile());
            ServletOutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * wang-editor 文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(HttpServletRequest request, MultipartFile  file){
        try {
            com.example.utils.AdminControllerUtils.requireAdmin(request);
        } catch (RuntimeException e) {
            return wangError("无权限上传文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            Map<String, Object> errMap = new HashMap<>();
            errMap.put("errno", 1);
            errMap.put("message", "文件名为空");
            return errMap;
        }
        String ext = getExt(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            Map<String, Object> errMap = new HashMap<>();
            errMap.put("errno", 1);
            errMap.put("message", "不支持的文件类型");
            return errMap;
        }
        String safeName = safeFileName(originalFilename);
        String fileName = java.util.UUID.randomUUID() + (ext.isBlank() ? "" : ("." + ext));
        try {
            FileUtil.writeBytes(file.getBytes(), FILE_DIR + fileName);
            System.out.println(fileName+ "--上传成功");
            Thread.sleep(1L);
        } catch (Exception e){
            System.out.println(fileName+ "--上传失败");
            return wangError("文件上传失败");
        }
        String http = fileBaseUrl +":" + port + "/files/download/" ;
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("errno", 0);
        resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", http + fileName)));
        return resMap;
    }

    private static String getExt(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase();
    }

    private static String safeFileName(String originalFilename) {
        String base = originalFilename;
        int index = originalFilename.lastIndexOf('.');
        if (index > 0) {
            base = originalFilename.substring(0, index);
        }
        String safeBase = base.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String ext = getExt(originalFilename);
        if (ext.isBlank()) {
            return safeBase;
        }
        return safeBase + "." + ext;
    }

    private static Map<String, Object> wangError(String message) {
        Map<String, Object> errMap = new HashMap<>();
        errMap.put("errno", 1);
        errMap.put("message", message);
        return errMap;
    }
}
