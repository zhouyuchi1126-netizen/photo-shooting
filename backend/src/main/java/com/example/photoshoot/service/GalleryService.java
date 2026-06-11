package com.example.photoshoot.service;

import com.example.photoshoot.mapper.AlbumMapper;
import com.example.photoshoot.mapper.ImageMapper;
import com.example.photoshoot.model.Album;
import com.example.photoshoot.model.GalleryGroup;
import com.example.photoshoot.model.Image;
import com.example.photoshoot.storage.StorageService;
import com.example.photoshoot.util.ExifUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GalleryService {

    private static final byte[][] MAGIC_BYTES = {
        {(byte)0xFF, (byte)0xD8, (byte)0xFF},
        {(byte)0x89, 0x50, 0x4E, 0x47},
        {0x42, 0x4D},
        {0x49, 0x49, 0x2A, 0x00}, {0x4D, 0x4D, 0x00, 0x2A}
    };

    private final AlbumMapper albumMapper;
    private final ImageMapper imageMapper;
    private final StorageService storage;

    public GalleryService(AlbumMapper albumMapper, ImageMapper imageMapper, StorageService storage) {
        this.albumMapper = albumMapper;
        this.imageMapper = imageMapper;
        this.storage = storage;
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
        return images.stream().map(Image::getFilepath).collect(Collectors.toList());
    }

    /* ========== 相册 CRUD ========== */

    public GalleryGroup createGroup(String groupId, String title,
                                     String cameraBrand, String cameraModel, Boolean isFilm, String filmStock) {
        Album album = new Album();
        album.setId(groupId);
        album.setTitle(title != null && !title.isBlank() ? title : formatTitle(groupId));
        album.setCameraBrand(cameraBrand);
        album.setCameraModel(cameraModel);
        album.setFilm(isFilm != null && isFilm);
        album.setFilmStock(album.isFilm() ? filmStock : null);
        albumMapper.insert(album);
        return toGroup(album);
    }

    public GalleryGroup updateGroup(String groupId, String title, String newGroupId,
                                     String cameraBrand, String cameraModel, Boolean isFilm, String filmStock) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");
        if (title != null && !title.isBlank()) album.setTitle(title);
        if (cameraBrand != null) album.setCameraBrand(cameraBrand);
        if (cameraModel != null) album.setCameraModel(cameraModel);
        if (isFilm != null) { album.setFilm(isFilm); album.setFilmStock(isFilm ? filmStock : null); }

        // 如果 groupId 变了，重命名文件夹并更新数据库中的 ID
        if (newGroupId != null && !newGroupId.equals(groupId) && !newGroupId.isBlank()) {
            storage.renameGroup(groupId, newGroupId);
            imageMapper.updateAlbumId(groupId, newGroupId);
            imageMapper.updateFilepathPrefix("/images/" + groupId, "/images/" + newGroupId);
            albumMapper.updateId(groupId, newGroupId);
            album.setId(newGroupId);
        }

        albumMapper.update(album);
        return toGroup(album);
    }

    public void deleteGroup(String groupId) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");
        imageMapper.deleteByAlbumId(groupId);
        albumMapper.deleteById(groupId);
        storage.deleteGroup(groupId);
    }

    /* ========== 图片操作 ========== */

    public void saveImage(String groupId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
        if (file.getSize() > 20 * 1024 * 1024)
            throw new IllegalArgumentException("文件大小不能超过 20MB");

        // 魔术字节校验
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 2 || java.util.Arrays.stream(MAGIC_BYTES).noneMatch(
                    magic -> java.util.Arrays.equals(magic, java.util.Arrays.copyOf(header, magic.length)))) {
                throw new IllegalArgumentException("不支持的文件格式（仅 JPEG/PNG/BMP/TIFF）");
            }
        } catch (IllegalArgumentException e) { throw e;
        } catch (IOException e) { throw new RuntimeException("无法读取文件", e); }

        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");

        // 通过 StorageService 保存
        String filepath = storage.save(groupId, file);
        String filename = filepath.substring(filepath.lastIndexOf('/') + 1);

        // 提取 EXIF 拍摄时间（仅第一张图片）
        if (album.getShootDate() == null || album.getShootDate().isBlank()) {
            try (InputStream is = file.getInputStream()) {
                java.time.LocalDateTime dt = ExifUtil.extractShootDate(is);
                if (dt != null) {
                    album.setShootDate(dt.toString());
                    albumMapper.update(album);
                }
            } catch (IOException ignored) {}
        }

        Image image = new Image();
        image.setId(UUID.randomUUID().toString());
        image.setAlbumId(groupId);
        image.setFilename(filename);
        image.setFilepath(filepath);
        imageMapper.insert(image);
    }

    public void deleteImage(String groupId, String filename) {
        List<Image> images = imageMapper.selectByAlbumId(groupId);
        Image img = images.stream().filter(i -> i.getFilename().equals(filename)).findFirst().orElse(null);
        if (img == null) throw new IllegalArgumentException("指定图片不存在");

        imageMapper.deleteByFilename(groupId, filename);
        storage.delete(groupId, filename);
    }

    public void reorderImages(String groupId, List<String> filenames) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");
        for (int i = 0; i < filenames.size(); i++) {
            imageMapper.updateSortOrder(groupId, filenames.get(i), i);
        }
    }

    public GalleryGroup setCover(String groupId, String filename) {
        Album album = albumMapper.selectById(groupId);
        if (album == null) throw new IllegalArgumentException("指定相册不存在");

        boolean exists = imageMapper.selectByAlbumId(groupId).stream()
                .anyMatch(i -> i.getFilename().equals(filename));
        if (!exists) throw new IllegalArgumentException("指定图片不存在");

        album.setCoverImage(filename);
        albumMapper.update(album);
        return toGroup(album);
    }

    /* ========== 工具 ========== */

    private GalleryGroup toGroup(Album album) {
        GalleryGroup group = new GalleryGroup();
        group.setId(album.getId());
        group.setTitle(album.getTitle());
        group.setShootDate(album.getShootDate());
        group.setCameraBrand(album.getCameraBrand());
        group.setCameraModel(album.getCameraModel());
        group.setFilm(album.isFilm());
        group.setFilmStock(album.getFilmStock());

        List<Image> images = imageMapper.selectByAlbumId(album.getId());
        group.setImageCount(images != null ? images.size() : 0);

        String cover = album.getCoverImage();
        if (cover != null && !cover.isBlank() && images != null &&
                images.stream().anyMatch(i -> i.getFilename().equals(cover))) {
            group.setCoverImage("/images/" + album.getId() + "/" + cover);
        } else if (images != null && !images.isEmpty()) {
            group.setCoverImage(images.get(0).getFilepath());
        }

        // 前 4 张预览（封面优先）
        if (images != null && !images.isEmpty()) {
            List<String> previews = images.stream().map(Image::getFilepath).collect(Collectors.toList());
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
