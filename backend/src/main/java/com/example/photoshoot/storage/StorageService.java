package com.example.photoshoot.storage;

import org.springframework.web.multipart.MultipartFile;

/** 图片存储抽象接口：本地文件系统 / 阿里云 OSS 可无缝切换 */
public interface StorageService {

    /** 保存图片文件，返回可公开访问的 URL */
    String save(String groupId, MultipartFile file);

    /** 删除单张图片 */
    void delete(String groupId, String filename);

    /** 删除整个分组目录 */
    void deleteGroup(String groupId);

    /** 重命名分组目录 */
    void renameGroup(String oldGroupId, String newGroupId);
}
