package com.example.photoshoot.mapper;

import com.example.photoshoot.model.Image;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImageMapper {
    List<Image> selectByAlbumId(@Param("albumId") String albumId);
    void insert(Image image);
    void deleteByFilename(@Param("albumId") String albumId, @Param("filename") String filename);
    void deleteByAlbumId(@Param("albumId") String albumId);
}
