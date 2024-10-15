/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.file;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.cowave.commons.tools.AssertsException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author shanhuiming
 *
 * <p>spring.servlet.multipart.location: ${java.io.tmpdir} # 临时目录
 * <p>spring.servlet.multipart.max-file-size: 20MB         # 单个文件最大为20M
 * <p>spring.servlet.multipart.max-request-size: 20MB      # 单次请求文件总大小为20M
 */
@RequiredArgsConstructor
@Service
public class FileService {

    @Nullable
    private final MinioService minioService;

    // 图片、office、压缩文件、视频、pdf
    @Value("#{'${spring.servlet.multipart.limits:bmp,gif,jpg,jpeg,png,doc,docx,xls,xlsx,ppt,pptx,html,htm,txt,rar,zip,gz,bz2,mp4,avi,rmvb,pdf,blob}'.split(',')}")
    private String[] fileLimits;

    private boolean suffixValid(String suffix) {
        suffix = suffix.toLowerCase();
        for (String str : fileLimits) {
            if (str.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 上传本地文件
     *
     * @param multipartFile 上传文件
     * @param dirPath       本地目录
     */
    public String localUpload(MultipartFile multipartFile, String dirPath) throws Exception {
        String name = multipartFile.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(name);
        if (!suffixValid(suffix)) {
            throw new AssertsException("{frame.file.invalid}", name);
        }

        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new AssertsException("{frame.dir.failed}", dirPath);
        }

        String md5 = DigestUtils.md5Hex(multipartFile.getBytes());
        String filePath = dirPath + File.separator + name + "." + md5;
        multipartFile.transferTo(Paths.get(filePath));
        return filePath;
    }

    /**
     * 下载本地文件
     *
     * @param filename 文件名称
     * @param filePath 本地路径
     */
    public void localDownload(HttpServletResponse resp, String filename, String filePath) throws IOException {
        filename = URLEncoder.encode(filename, "UTF-8").replace("\\+", "%20");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/octet-stream;charset=UTF-8");
        resp.setHeader("Content-disposition", "attachment;filename*=utf-8''" + filename + "\"");
        try (FileInputStream fileInput = new FileInputStream(filePath);
             OutputStream outPut = resp.getOutputStream();) {
            IOUtils.copy(fileInput, outPut);
        }
    }

    /**
     * 上传Minio文件
     *
     * @param multipartFile 上传文件
     * @param bucket        文件分区
     * @param filePath      文件路径
     * @param isPublic      是否开放的
     */
    public void minioUpload(MultipartFile multipartFile, String bucket, String filePath, boolean isPublic) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        minioService.upload(multipartFile, filePath, bucket, isPublic);
    }

    /**
     * 上传Minio文件
     *
     * @param inputStream 输入流
     * @param bucket      文件分区
     * @param filePath    文件路径
     * @param isPublic    是否开放的
     */
    public void minioUpload(InputStream inputStream, String bucket, String filePath, boolean isPublic) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        minioService.upload(inputStream, filePath, bucket, isPublic);
    }

    /**
     * 下载Minio文件
     *
     * @param bucket   文件分区
     * @param filePath 文件路径
     * @param fileName 文件名称
     */
    public void minioDownload(HttpServletResponse resp, String bucket, String filePath, String fileName) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        minioService.download(resp, bucket, filePath, fileName);
    }

    /**
     * 预览Minio文件
     *
     * @param bucket   文件分区
     * @param filePath 文件路径
     */
    public String minioPreview(String bucket, String filePath) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        return minioService.preview(bucket, filePath);
    }

    /**
     * 预览Minio文件
     *
     * @param bucket   文件分区
     * @param filePath 文件路径
     * @param expireSeconds 超时时间
     */
    public String minioPreview(String bucket, String filePath, int expireSeconds) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        return minioService.preview(bucket, filePath, expireSeconds);
    }

    /**
     * 删除Minio文件
     *
     * @param bucket   文件分区
     * @param filePath 文件路径
     */
    public void minioDelete(String bucket, String filePath) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        minioService.delete(bucket, filePath);
    }

    /**
     * minio获取InputStream
     *
     * @author cailinfei
     */
    public InputStream minioInputStream(String bucket, String filePath) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        return minioService.getInputStream(bucket, filePath);
    }

    /**
     * 多文件压缩为tgz下载
     *
     * @author cailinfei
     */
    public byte[] minioDownloadTgzBytes(Map<String, String> filePathMap, String bucket) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOs = new TarArchiveOutputStream(new GZIPOutputStream(byteArrayOutputStream))) {
            minioService.appendToTgz(tarOs, filePathMap, bucket);
            tarOs.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * 多文件压缩为tgz下载
     *
     * @author cailinfei
     */
    public void minioDownloadTgz(HttpServletResponse resp, String bucket, Map<String, String> filePathMap, String fileName) throws Exception {
        if (minioService == null) {
            throw new AssertsException("{frame.not.exist.minio}");
        }
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/octet-stream;charset=UTF-8");
        resp.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);
        try (TarArchiveOutputStream tarOs = new TarArchiveOutputStream(new GZIPOutputStream(resp.getOutputStream()))) {
            minioService.appendToTgz(tarOs, filePathMap, bucket);
            tarOs.finish();
        }
    }
}
