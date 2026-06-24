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

    // 表示本地磁盘文件的存储路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

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
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error("文件名为空");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error("不支持的文件类型");
        }
        // 清理文件名，防止路径遍历
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        // 定义文件的唯一标识
        String fileName = System.currentTimeMillis() + "-" + safeName;
        // 拼接完整的文件存储路径
        String realFilePath = filePath + fileName;
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            FileUtil.writeBytes(file.getBytes(), realFilePath);
        } catch (IOException e) {
            System.out.println("文件上传错误");
        }

        // 返回文件下载的地址
        String url = fileBaseUrl + ":" + port + "/files/download/" + fileName;
        return Result.success(url);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        // 设置下载文件http响应头
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        // 拼接完整的文件存储路径
        String realFilePath = filePath + fileName;
        try {
            // 通过文件的存储路径拿到文件字节数组
            byte[] bytes = FileUtil.readBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            // 将文件字节数组写出到文件流
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println("文件下载错误");
        }
    }

    /**
     * wang-editor 文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile  file){
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            Map<String, Object> errMap = new HashMap<>();
            errMap.put("errno", 1);
            errMap.put("message", "文件名为空");
            return errMap;
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            Map<String, Object> errMap = new HashMap<>();
            errMap.put("errno", 1);
            errMap.put("message", "不支持的文件类型");
            return errMap;
        }
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String fileName = System.currentTimeMillis() + "-" + safeName;
        try {
            FileUtil.writeBytes(file.getBytes(), filePath + fileName);
            System.out.println(fileName+ "--上传成功");
            Thread.sleep(1L);
        } catch (Exception e){
            System.out.println(fileName+ "--上传失败");
        }
        String http = fileBaseUrl +":" + port + "/files/download/" ;
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("errno", 0);
        resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", http + fileName)));
        return resMap;
    }
}
