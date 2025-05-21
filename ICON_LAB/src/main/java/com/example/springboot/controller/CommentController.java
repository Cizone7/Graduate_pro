package com.example.springboot.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Comment;
import com.example.springboot.entity.Creation;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICommentService;
import com.example.springboot.service.ICreationService;
import com.example.springboot.service.ILogService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private ICommentService commentService;
    @Resource
    private IUserService userService;

    @Resource
    private ILogService logService;

    @Resource
    private ICreationService creationService;
    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Comment comment, HttpServletRequest request) {
        if (comment.getId() == null) { // 新增评论
            comment.setUserId(TokenUtils.getCurrentUser().getId());
            comment.setTime(DateUtil.now());
            if (comment.getPid() != null) {
                Integer pid = comment.getPid();
                Comment pComment = commentService.getById(pid);
                if (pComment.getOriginId() != null) {
                    comment.setOriginId(pComment.getOriginId());
                } else {
                    comment.setOriginId(comment.getPid());
                }
            }
        }
        commentService.saveOrUpdate(comment);

        Log log = new Log();
        log.setUserId(comment.getUserId());
        log.setOperationType("COMMENT");
        log.setOperationDetail("用户评论作品："+comment.getItemId());
        log.setIp(request.getRemoteAddr());
        logService.save(log);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id,HttpServletRequest request) {
        commentService.removeById(id);
        Log log = new Log();
        log.setUserId(TokenUtils.getCurrentUser().getId());
        log.setOperationType("DEL_COMMENT");
        log.setOperationDetail("用户删除评论id："+id);
        log.setIp(request.getRemoteAddr());
        logService.save(log);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        commentService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(commentService.list());
    }

    @AuthAccess
    @GetMapping("/tree/{itemId}")
    public Result findTree(@PathVariable Integer itemId) {
        Map<Integer, User> map = userService.list().stream().collect(Collectors.toMap(User::getId, u -> u));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getItemId, itemId);
        wrapper.orderByDesc(Comment::getId);
        List<Comment> allComments = commentService.list(wrapper).stream()
                .map(c -> {
                    Optional.ofNullable(map.get(c.getUserId())).ifPresent(user -> {
                        c.setNickname(user.getNickname());
                        c.setAvatarUrl(user.getAvatarUrl());
                    });
                    return c;
                })
                .collect(Collectors.toList());
        List<Comment> originList = allComments.stream().filter(comment -> comment.getOriginId() == null).collect(Collectors.toList());
        for (Comment origin : originList) {
            List<Comment> comments = allComments.stream().filter(comment -> origin.getId().equals(comment.getOriginId())).collect(Collectors.toList());  // 表示回复对象集合
            comments.forEach(comment -> {
                Optional<Comment> pComment = allComments.stream().filter(c1 -> c1.getId().equals(comment.getPid())).findFirst();
                pComment.ifPresent((v -> {
                    comment.setPUserId(v.getUserId());
                    comment.setPNickname(v.getNickname());
                }));
            });
            origin.setChildren(comments);
        }
        return Result.success(originList);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(commentService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return Result.success(commentService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/notifications")
    public Result getNotifications() {
        try {
            User currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("401", "未登录");
            }

            List<Comment> comments = commentService.getCommentsByAuthor(currentUser.getId());
            if (comments == null) comments = new ArrayList<>();

            List<Comment> validComments = new ArrayList<>();
            for (Comment c : comments) {
                if (c.getPid() != null) {
                    Comment parent = commentService.getById(c.getPid());
                    if (parent == null) {
                        continue;
                    }
                }
                validComments.add(c);
            }

            List<Integer> userIds = validComments.stream()
                    .map(Comment::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Integer, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> userList = userService.listByIds(userIds);
                for (User user : userList) {
                    userMap.put(user.getId(), user);
                }
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Comment c : validComments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("content", c.getContent());
                map.put("time", c.getTime());
                map.put("isRead", c.getIsRead());
                map.put("itemId", c.getItemId());
                map.put("itemName", getItemName(c.getItemId()));

                User user = userMap.get(c.getUserId());
                if (user != null) {
                    map.put("nickname", user.getNickname());
                    map.put("avatarUrl", user.getAvatarUrl());
                } else {
                    map.put("nickname", "未知用户");
                    map.put("avatarUrl", null);
                }

                result.add(map);
            }

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "服务器内部错误：" + e.getMessage());
        }
    }



    private String getItemName(Integer itemId) {
        if (itemId == null) {
            return "未知作品"; // 处理 itemId 为 null 的情况
        }
        Creation creation = creationService.getById(itemId);
        return creation != null ? creation.getName() : "未知作品";
    }

    @PostMapping("/read")
    public Result markAsRead(@RequestBody List<Integer> ids) {
        if (ids.isEmpty()) return Result.success();

        commentService.update(new LambdaUpdateWrapper<Comment>()
                .set(Comment::getIsRead, true)
                .in(Comment::getId, ids));

        return Result.success();
    }


}


