package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Comment;
import com.example.springboot.entity.Type;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * 
 */
public interface ICommentService extends IService<Comment> {
    List<Comment> getCommentsByAuthor(Integer userId);

}
