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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import io.minio.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.multipart.MultipartFile;

import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@ConditionalOnClass(MinioClient.class)
class MinioService {

    private final MinioClient minioClient;

    private static final String POLICY_PUBLIC = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::%s\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}";

    public boolean existBucket(String bucket) throws Exception{
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    }

    public void makeBucket(String bucket, boolean isPublic) throws Exception{
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        if(isPublic){
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket)
                    .config(String.format(POLICY_PUBLIC, bucket, bucket)).build());
        }
    }

    public void deleteBucket(String bucket) throws Exception{
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
    }

    public List<Bucket> listBuckets() throws Exception{
        return minioClient.listBuckets();
    }

    /**
     * 文本文件使用编码UTG-8-BOM，否则预览存在乱码
     */
    public void upload(MultipartFile file, String fileName, String bucket, boolean isPublic) throws Exception{
        if(!existBucket(bucket)) {
            makeBucket(bucket, isPublic);
        }
        PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucket).object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
        minioClient.putObject(objectArgs);
    }

    public void upload(InputStream stream, String fileName, String bucket, boolean isPublic) throws Exception {
        if (!existBucket(bucket)) {
            makeBucket(bucket, isPublic);
        }
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(fileName)
                .stream(stream, stream.available(), -1).build());
    }

    public void download(HttpServletResponse resp, String bucket, String filePath, String fileName) throws Exception{
        fileName = URLEncoder.encode(fileName, "UTF-8").replace("\\+", "%20");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/octet-stream;charset=UTF-8");
        resp.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + "\"");
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs);
                OutputStream outPut = resp.getOutputStream()){
            IOUtils.copy(response, outPut);
        }
    }

    public String preview(String bucket, String filePath) throws Exception{
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder().bucket(bucket).object(filePath).method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    public String preview(String bucket, String filePath, int expireSeconds) throws Exception{
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder().bucket(bucket).object(filePath).method(Method.GET).expiry(expireSeconds).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    public List<Item> list(String bucket) throws Exception{
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).build());
        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }
        return items;
    }

    public void delete(String bucket, String filePath) throws Exception{
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(filePath).build());
    }

    public InputStream getInputStream(String bucket, String filePath) throws Exception {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();
        return minioClient.getObject(objectArgs);
    }

    public void appendToTgz(TarArchiveOutputStream tarOs, Map<String, String> filePathMap, String bucket) throws Exception {
        tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        for (Map.Entry<String, String> entry : filePathMap.entrySet()) {
            try(GetObjectResponse response =  minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(entry.getValue()).build())){
                TarArchiveEntry tarEntry = new TarArchiveEntry(entry.getKey());
                tarEntry.setSize(Long.parseLong(Objects.requireNonNull(response.headers().get("Content-Length"))));
                tarOs.putArchiveEntry(tarEntry);
                org.apache.commons.compress.utils.IOUtils.copy(response, tarOs);
                tarOs.flush();
                tarOs.closeArchiveEntry();
            }
        }
    }
}
