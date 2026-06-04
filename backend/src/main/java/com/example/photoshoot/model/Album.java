package com.example.photoshoot.model;

public class Album {
    private String id;
    private String title;
    private String coverImage;
    private String createdAt;
    private String shootDate;
    private String cameraBrand;
    private String cameraModel;
    private boolean isFilm;
    private String filmStock;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
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
