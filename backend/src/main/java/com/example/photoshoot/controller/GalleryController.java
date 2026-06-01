package com.example.photoshoot.controller;

import com.example.photoshoot.model.GalleryGroup;
import com.example.photoshoot.service.GalleryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping("/groups")
    public List<GalleryGroup> getGroups() {
        return galleryService.listGroups();
    }

    @GetMapping("/groups/{groupId}/images")
    public List<String> getImages(@PathVariable String groupId) {
        return galleryService.listImages(groupId);
    }
}
