package com.example.photoshoot.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 存储（生产部署推荐）
 *
 * 启用方式：application.properties 中配置：
 *   app.storage.type=oss
 *   app.storage.oss.endpoint=https://oss-cn-hangzhou.aliyuncs.com
 *   app.storage.oss.access-key-id=xxx
 *   app.storage.oss.access-key-secret=xxx
 *   app.storage.oss.bucket-name=my-photo-bucket
 *   app.storage.oss.domain=https://my-photo-bucket.oss-cn-hangzhou.aliyuncs.com
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oss")
public class OssStorageService implements StorageService {

    private final OSS ossClient;
    private final String bucket;
    private final String domain;      // 自定义域名 or OSS 默认域名

    public OssStorageService(
            @Value("${app.storage.oss.endpoint}") String endpoint,
            @Value("${app.storage.oss.access-key-id}") String ak,
            @Value("${app.storage.oss.access-key-secret}") String sk,
            @Value("${app.storage.oss.bucket-name}") String bucket,
            @Value("${app.storage.oss.domain}") String domain) {
        this.ossClient = new OSSClientBuilder().build(endpoint, ak, sk);
        this.bucket = bucket;
        this.domain = domain;
    }

    @Override
    public String save(String groupId, MultipartFile file) {
        String ext = extractExt(file.getOriginalFilename());
        String objectKey = groupId + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream input = file.getInputStream()) {
            ossClient.putObject(bucket, objectKey, input);
        } catch (Exception e) {
            throw new RuntimeException("OSS 上传失败", e);
        }
        return domain + "/" + objectKey;
    }

    @Override
    public void delete(String groupId, String filename) {
        // filename 可能是 "uuid.jpg" 或完整 objectKey
        String objectKey = filename.contains("/") ? filename : groupId + "/" + filename;
        ossClient.deleteObject(bucket, objectKey);
    }

    @Override
    public void renameGroup(String oldGroupId, String newGroupId) {
        String oldPrefix = oldGroupId + "/";
        String newPrefix = newGroupId + "/";
        var objects = ossClient.listObjects(bucket, oldPrefix);
        for (var summary : objects.getObjectSummaries()) {
            String newKey = newPrefix + summary.getKey().substring(oldPrefix.length());
            ossClient.copyObject(bucket, summary.getKey(), bucket, newKey);
            ossClient.deleteObject(bucket, summary.getKey());
        }
    }

    @Override
    public void deleteGroup(String groupId) {
        // 列出所有以 groupId/ 开头的文件并批量删除
        String prefix = groupId + "/";
        var objects = ossClient.listObjects(bucket, prefix);
        objects.getObjectSummaries().stream()
                .map(com.aliyun.oss.model.OSSObjectSummary::getKey)
                .forEach(key -> ossClient.deleteObject(bucket, key));
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) ossClient.shutdown();
    }
}
