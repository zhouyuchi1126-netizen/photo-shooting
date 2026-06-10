package com.example.photoshoot.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/** 本地文件系统存储（开发 / 未配置 OSS 时默认使用） */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(@Value("${app.storage.local.path:images}") String path) {
        this.root = Paths.get(path);
    }

    @PostConstruct
    public void init() {
        try { Files.createDirectories(root); } catch (IOException e) {
            throw new RuntimeException("无法创建图片目录: " + root, e);
        }
    }

    private Path groupDir(String groupId) {
        return root.resolve(groupId);
    }

    @Override
    public String save(String groupId, MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) filename = java.util.UUID.randomUUID() + ".jpg";

        Path folder = groupDir(groupId);
        try {
            Files.createDirectories(folder);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, folder.resolve(filename));
            }
        } catch (IOException e) {
            throw new RuntimeException("无法保存图片文件", e);
        }
        return "/images/" + groupId + "/" + filename;
    }

    @Override
    public void delete(String groupId, String filename) {
        try {
            Files.deleteIfExists(groupDir(groupId).resolve(filename));
        } catch (IOException ignored) {}
    }

    @Override
    public void deleteGroup(String groupId) {
        Path folder = groupDir(groupId);
        if (Files.exists(folder)) {
            try {
                Files.walk(folder)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            } catch (IOException ignored) {}
        }
    }
}
