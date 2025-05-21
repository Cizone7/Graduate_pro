package com.example.springboot.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.User;
import com.example.springboot.service.ILogService;
import com.example.springboot.service.IUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 *
 */
@RestController
@RequestMapping("/sys-log")
public class LogController {

    @Resource
    private ILogService logService;
    @Resource
    private IUserService userService;

    // 分页查询日志
    @GetMapping("/page")
    public Result findPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(defaultValue = "") String operationType,
            @RequestParam(defaultValue = "") String nickname,
            @RequestParam(required = false) Integer userId) {

        QueryWrapper<Log> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");

        if (!operationType.isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        } else if (!nickname.isEmpty()) {
            // 联表查询用户昵称
            if (!nickname.isEmpty()) {
                List<Integer> userIds = userService.list(new QueryWrapper<User>().like("nickname", nickname))
                        .stream().map(User::getId).collect(Collectors.toList());
                if (!userIds.isEmpty()) {
                    queryWrapper.in("user_id", userIds);
                } else {
                    return Result.success(new Page<>(pageNum, pageSize, 0));
                }
            }
        }


        return Result.success(logService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    // 统计BE_DOWNLOADED操作的用户下载次数（按作者分组）
    @GetMapping("/downloadStats")
    public Result getDownloadStats() {
        // 1. 查询所有 BE_DOWNLOADED 日志
        QueryWrapper<Log> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "BE_DOWNLOADED");
        List<Log> logs = logService.list(queryWrapper);

        // 2. 按userId分组统计次数
        Map<Integer, Long> userIdCountMap = logs.stream()
                .collect(Collectors.groupingBy(Log::getUserId, Collectors.counting()));

        // 3. 关联用户表获取用户名
        List<Map<String, Object>> result = new ArrayList<>();
        userIdCountMap.forEach((userId, count) -> {
            User user = userService.getById(userId);
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", user.getNickname());
                item.put("value", count);
                result.add(item);
            }
        });

        // 4. 按下载次数降序排序，取TopN
        result.sort((a, b) -> (int) ((Long)b.get("value") - (Long)a.get("value")));

        return Result.success(result);
    }

}
