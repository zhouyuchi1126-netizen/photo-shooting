package com.example.photoshoot.mapper;

import com.example.photoshoot.model.Album;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlbumMapper {
    List<Album> selectAll();
    Album selectById(@Param("id") String id);
    void insert(Album album);
    void update(Album album);
    void deleteById(@Param("id") String id);
    void updateId(@Param("oldId") String oldId, @Param("newId") String newId);
}
