package com.example.photoshoot.mapper;

import com.example.photoshoot.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectByUsername(String username);
    User selectByWechatOpenid(String wechatOpenid);
    int insertUser(User user);
    int updateWechatOpenid(User user);
}
