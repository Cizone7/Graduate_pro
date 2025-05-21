package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Comment;
import com.example.springboot.entity.Type;
import com.example.springboot.entity.Creation;
import com.example.springboot.mapper.CommentMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.service.ICommentService;
import com.example.springboot.service.ICreationService;
import com.example.springboot.service.ITypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * 
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Resource
    private ICreationService creationService;

    @Override
    public List<Comment> getCommentsByAuthor(Integer userId) {
        try {
            List<Integer> itemIds = creationService.list(new LambdaQueryWrapper<Creation>()
                            .select(Creation::getId)
                            .eq(Creation::getUserId, userId))
                    .stream()
                    .map(Creation::getId)
                    .collect(Collectors.toList());

            if (itemIds.isEmpty()) {
                return Collections.emptyList();
            }

            return this.list(new LambdaQueryWrapper<Comment>()
                    .in(Comment::getItemId, itemIds)
                    .orderByDesc(Comment::getId));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


}
