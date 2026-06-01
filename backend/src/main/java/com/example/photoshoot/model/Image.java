package com.example.photoshoot.model;

public class Image {
    private String id;
    private String albumId;
    private String filename;
    private String filepath;
    private String uploadedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAlbumId() { return albumId; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getFilepath() { return filepath; }
    public void setFilepath(String filepath) { this.filepath = filepath; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
}
