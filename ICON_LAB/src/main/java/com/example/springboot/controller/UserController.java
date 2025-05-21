package com.example.springboot.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.controller.dto.UserPasswordDTO;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.User;
import com.example.springboot.service.ILogService;
import com.example.springboot.service.IUserService;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * 
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private ILogService logService;

     // 新增或者更新
    @PostMapping
    public Result save(@RequestBody User user) {
        String username = user.getUsername();
        if (StrUtil.isBlank(username)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        if (StrUtil.isBlank(user.getNickname())) {
            user.setNickname(username);
        }
        if (user.getId() != null) {
            user.setPassword(null);
        } else {
            if (user.getPassword() == null) {
                user.setPassword("123");
            }
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    /**
     * 修改密码
     *
     * @param userPasswordDTO
     * @return
     */
    @PostMapping("/password")
    public Result password(@RequestBody UserPasswordDTO userPasswordDTO) {
        userService.updatePassword(userPasswordDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/front/count")
    public Result findFrontCount() {
        // 构建联表查询，统计每个用户的作品数量
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sys_user.*, (SELECT COUNT(*) FROM creation WHERE creation.user_id = sys_user.id) AS creation_count");
        queryWrapper.eq("role", "ROLE_USER");
        queryWrapper.orderByDesc("creation_count"); // 按作品数量降序
        queryWrapper.last("LIMIT 6"); // 取前6名

        List<User> users = userService.list(queryWrapper);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Result.success(userService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String role,
                           @RequestParam(defaultValue = "") String nickname,
                           @RequestParam(defaultValue = "") String email,
                           @RequestParam(defaultValue = "") String address) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(role)) {
            queryWrapper.eq("role", role);
        }
        if (!"".equals(nickname)) {
            queryWrapper.like("nickname", nickname);
        }
        if (!"".equals(email)) {
            queryWrapper.like("email", email);
        }
        if (!"".equals(address)) {
            queryWrapper.like("address", address);
        }

        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


    @PutMapping("/password-reset/{id}")
    public Result resetPassword(@PathVariable Integer id, HttpServletRequest request) {
        userService.resetPassword(id);
        Log log = new Log();
        log.setUserId(id);
        log.setOperationType("RESET_PASSWORD");
        log.setOperationDetail("用户重置密码");
        log.setIp(request.getRemoteAddr());
        logService.save(log);
        return Result.success();
    }

}

