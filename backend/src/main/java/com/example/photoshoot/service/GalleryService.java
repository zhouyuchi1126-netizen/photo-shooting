package com.example.photoshoot.service;

import com.example.photoshoot.mapper.AlbumMapper;
import com.example.photoshoot.mapper.ImageMapper;
import com.example.photoshoot.model.Album;
import com.example.photoshoot.model.GalleryGroup;
import com.example.photoshoot.model.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GalleryService {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private final Path rootDirectory = Paths.get("images");
    private final AlbumMapper albumMapper;
    private final ImageMapper imageMapper;

    public GalleryService(AlbumMapper albumMapper, ImageMapper imageMapper) {
        this.albumMapper = albumMapper;
        this.imageMapper = imageMapper;
        try {
            Files.createDirectories(rootDirectory);
        } catch (IOException e) {
            throw new RuntimeException("无法创建图片存储目录", e);
        }
    }

    /* ========== 查询 ========== */

    public List<GalleryGroup> listGroups() {
        List<Album> albums = albumMapper.selectAll();
        if (albums == null || albums.isEmpty()) return Collections.emptyList();
        return albums.stream().map(this::toGroup).collect(Collectors.toList());
    }

    public List<String> listImages(String groupId) {
        List<Image> images = imageMapper.selectByAlbumId(groupId);
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream()
                .map(img -> "/images/" + groupId + "/" + img.getFilename())
                .collect(Collectors.toList());
    }

    /* ========== 创建/更新/删除 相册 ========== */

    public GalleryGroup createGroup(String groupId, String title, String description) {
        Album album = new Album();
        album.setId(groupId);
        album.setTitle(title != null && !title.isBlank() ? title : formatTitle(groupId));
        album.setDescription(description != null && !description.isBlank() ? description : "由管理员维护的作品集");
        albumMapper.insert(album);
        // 创建磁盘目录
        try {
            Files.createDirectories(rootDirectory.resolve(groupId));
        } catch (IOException e) {
            throw new RuntimeException("无法创建图片目录", e);
        }
        return toGroup(album);
    }

    public GalleryGroup updateGroup(String groupId, String title, String description) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");
        if (title != null && !title.isBlank()) album.setTitle(title);
        if (description != null && !description.isBlank()) album.setDescription(description);
        albumMapper.update(album);
        return toGroup(album);
    }

    public void deleteGroup(String groupId) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");
        imageMapper.deleteByAlbumId(groupId);
        albumMapper.deleteById(groupId);
        // 删除磁盘目录及文件
        Path folder = rootDirectory.resolve(groupId);
        if (Files.exists(folder)) {
            try {
                Files.walk(folder)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            } catch (IOException ignored) {}
        }
    }

    /* ========== 图片操作 ========== */

    public void saveImage(String groupId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) filename = UUID.randomUUID() + ".jpg";

        Path folder = rootDirectory.resolve(groupId);
        try {
            Files.createDirectories(folder);
            Path dest = folder.resolve(filename);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, dest);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法保存图片文件", e);
        }

        Image image = new Image();
        image.setId(UUID.randomUUID().toString());
        image.setAlbumId(groupId);
        image.setFilename(filename);
        image.setFilepath("/images/" + groupId + "/" + filename);
        imageMapper.insert(image);
    }

    public void deleteImage(String groupId, String filename) {
        Image img = imageMapper.selectByAlbumId(groupId).stream()
                .filter(i -> i.getFilename().equals(filename))
                .findFirst().orElse(null);
        if (img == null) throw new IllegalArgumentException("指定图片不存在");

        imageMapper.deleteByFilename(groupId, filename);

        Path filePath = rootDirectory.resolve(groupId).resolve(filename);
        try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
    }

    public GalleryGroup setCover(String groupId, String filename) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");

        // 检查图片是否存在
        List<Image> images = imageMapper.selectByAlbumId(groupId);
        boolean exists = images.stream().anyMatch(i -> i.getFilename().equals(filename));
        if (!exists) throw new IllegalArgumentException("指定图片不存在");

        album.setCoverImage(filename);
        albumMapper.update(album);
        return toGroup(album);
    }

    /* ========== 工具方法 ========== */

    private GalleryGroup toGroup(Album album) {
        GalleryGroup group = new GalleryGroup();
        group.setId(album.getId());
        group.setTitle(album.getTitle());
        group.setDescription(album.getDescription());

        List<Image> images = imageMapper.selectByAlbumId(album.getId());
        group.setImageCount(images != null ? images.size() : 0);

        // 封面
        String cover = album.getCoverImage();
        if (cover != null && !cover.isBlank() && images != null &&
                images.stream().anyMatch(i -> i.getFilename().equals(cover))) {
            group.setCoverImage("/images/" + album.getId() + "/" + cover);
        } else if (images != null && !images.isEmpty()) {
            group.setCoverImage("/images/" + album.getId() + "/" + images.get(0).getFilename());
        }

        // 前 4 张预览（封面优先）
        if (images != null && !images.isEmpty()) {
            List<String> previews = images.stream()
                    .map(i -> "/images/" + album.getId() + "/" + i.getFilename())
                    .collect(Collectors.toList());
            String coverUrl = group.getCoverImage();
            if (coverUrl != null && previews.contains(coverUrl)) {
                previews.remove(coverUrl);
                previews.add(0, coverUrl);
            }
            group.setPreviewImages(previews.stream().limit(4).collect(Collectors.toList()));
        }

        return group;
    }

    private String formatTitle(String id) {
        String[] parts = id.split("[-_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
