package com.example.photoshoot.controller;

import com.example.photoshoot.model.GalleryGroup;
import com.example.photoshoot.service.GalleryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class AdminController {

    private final GalleryService galleryService;

    public AdminController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping("/groups")
    public List<GalleryGroup> getGroups() {
        return galleryService.listGroups();
    }

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @RequestBody Map<String, String> payload) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        String groupId = payload.get("groupId");
        String title = payload.get("title");
        String cameraBrand = payload.get("cameraBrand");
        String cameraModel = payload.get("cameraModel");
        Boolean isFilm = payload.containsKey("isFilm") ? "true".equals(payload.get("isFilm")) : null;
        String filmStock = payload.get("filmStock");
        if (groupId == null || groupId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "分组 ID 不能为空"));
        }
        GalleryGroup group = galleryService.createGroup(groupId, title, cameraBrand, cameraModel, isFilm, filmStock);
        return ResponseEntity.ok(group);
    }

    @PostMapping(path = "/groups/{groupId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId,
            @RequestParam("file") MultipartFile file) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        try {
            galleryService.saveImage(groupId, file);
            return ResponseEntity.ok(Map.of("message", "上传成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "上传失败"));
        }
    }

    @PutMapping("/groups/{groupId}")
    public ResponseEntity<?> updateGroup(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId,
            @RequestBody Map<String, String> payload) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        try {
            Boolean isFilm = payload.containsKey("isFilm") ? "true".equals(payload.get("isFilm")) : null;
            GalleryGroup group = galleryService.updateGroup(groupId,
                    payload.get("title"), payload.get("newGroupId"),
                    payload.get("cameraBrand"), payload.get("cameraModel"), isFilm, payload.get("filmStock"));
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        try {
            galleryService.deleteGroup(groupId);
            return ResponseEntity.ok(Map.of("message", "分组已删除"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/groups/{groupId}/images/{imageName}")
    public ResponseEntity<?> deleteImage(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId,
            @PathVariable String imageName) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        try {
            galleryService.deleteImage(groupId, imageName);
            return ResponseEntity.ok(Map.of("message", "图片已删除"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/groups/{groupId}/cover")
    public ResponseEntity<?> setCover(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId,
            @RequestBody Map<String, String> payload) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        String imageName = payload.get("imageName");
        if (imageName == null || imageName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "图片名称不能为空"));
        }
        try {
            GalleryGroup group = galleryService.setCover(groupId, imageName);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/groups/{groupId}/images/sort")
    public ResponseEntity<?> reorderImages(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @PathVariable String groupId,
            @RequestBody Map<String, Object> payload) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        @SuppressWarnings("unchecked")
        List<String> filenames = (List<String>) payload.get("filenames");
        if (filenames == null || filenames.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "图片列表不能为空"));
        }
        try {
            galleryService.reorderImages(groupId, filenames);
            return ResponseEntity.ok(Map.of("message", "排序已保存"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/groups/reorder")
    public ResponseEntity<?> reorderGroups(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role,
            @RequestBody Map<String, Object> payload) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权限"));
        }
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) payload.get("groupIds");
        if (groupIds == null || groupIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "相册列表不能为空"));
        }
        try {
            galleryService.reorderAlbums(groupIds);
            return ResponseEntity.ok(Map.of("message", "相册排序已保存"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
