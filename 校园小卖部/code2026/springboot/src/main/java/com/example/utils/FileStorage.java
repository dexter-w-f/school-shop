package com.example.utils;

import java.io.File;

/**
 * 上传文件的存储目录解析。
 *
 * 历史上 FileController 用 {@code System.getProperty("user.dir") + "/files/"}，
 * 而项目实际的 files 目录位于 code2026/files（与 springboot 同级），导致
 * 静态资源映射与上传目录不一致、图片 403/404。这里统一解析，供
 * FileController 与 WebConfig 共用。
 */
public final class FileStorage {

    /** 可通过 -Dfile.storage.dir=... 显式指定 */
    private static final String EXPLICIT_PROPERTY = "file.storage.dir";

    private FileStorage() {
    }

    /**
     * @return 以 / 结尾的绝对目录路径（目录可能尚不存在）
     */
    public static String baseDir() {
        String explicit = System.getProperty(EXPLICIT_PROPERTY);
        if (explicit != null && !explicit.isBlank()) {
            return ensureTrailingSlash(new File(explicit).getAbsolutePath());
        }
        String userDir = System.getProperty("user.dir");
        // 优先复用已存在的目录，兼容 bootRun（cwd=springboot）与 jar 部署（cwd=项目根）
        String[] candidates = {
                userDir + "/files",
                userDir + "/../files",
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.isDirectory()) {
                return ensureTrailingSlash(f.getAbsolutePath());
            }
        }
        // 都不存在时按历史默认值，交由上传逻辑自行创建
        return ensureTrailingSlash(new File(userDir + "/files").getAbsolutePath());
    }

    private static String ensureTrailingSlash(String path) {
        return path.endsWith("/") || path.endsWith("\\") ? path : path + "/";
    }
}
