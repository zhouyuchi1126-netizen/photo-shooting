package com.example.photoshoot.model;

import java.util.List;

public class GalleryGroup {
    private String id;
    private String title;
    private String coverImage;
    private int imageCount;
    private List<String> previewImages;
    private String shootDate;
    private String cameraBrand;
    private String cameraModel;
    private boolean isFilm;
    private String filmStock;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public List<String> getPreviewImages() {
        return previewImages;
    }

    public void setPreviewImages(List<String> previewImages) {
        this.previewImages = previewImages;
    }
    public String getShootDate() { return shootDate; }
    public void setShootDate(String shootDate) { this.shootDate = shootDate; }
    public String getCameraBrand() { return cameraBrand; }
    public void setCameraBrand(String cameraBrand) { this.cameraBrand = cameraBrand; }
    public String getCameraModel() { return cameraModel; }
    public void setCameraModel(String cameraModel) { this.cameraModel = cameraModel; }
    public boolean isFilm() { return isFilm; }
    public void setFilm(boolean film) { isFilm = film; }
    public String getFilmStock() { return filmStock; }
    public void setFilmStock(String filmStock) { this.filmStock = filmStock; }
}
