package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.controller.dto.LibraryDTO;
import com.example.springboot.entity.Library;
import com.example.springboot.entity.User;
import com.example.springboot.entity.Creation;
import com.example.springboot.service.ICreationService;
import com.example.springboot.service.ILibraryService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import cn.hutool.core.date.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器，用于管理图书馆相关操作
 * </p>
 *
 * @author
 */
@RestController
@RequestMapping("/library")
public class LibraryController {

    @Resource
    private ILibraryService libraryService;
    @Resource
    private IUserService userService;

    @Resource
    private ICreationService creationService;

    // 新增或更新信息
    @PostMapping
    public Result save(@RequestBody Library library) {
        User currentUser = TokenUtils.getCurrentUser();

        if (library.getId() == null) { // 新建操作
            // 显式设置初始值
            library.setCreationCount(0); // 新增代码
            library.setUserId(currentUser.getId());
            library.setTime(DateUtil.now());

            // 保存并返回
            libraryService.save(library);
            return Result.success(libraryService.getById(library.getId()));
        } else { // 更新操作
            // 更新时保留原有 creationCount 值
            Library existingLib = libraryService.getById(library.getId());
            library.setCreationCount(existingLib.getCreationCount()); // 防止覆盖
            libraryService.updateById(library);
            return Result.success(library);
        }
    }

    // 批量删除（根据ID列表）
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(libraryService.removeByIds(ids)); // 批量删除
    }

    // 删除单个图库（根据ID）
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            Library library = libraryService.getById(id);
            User currentUser = TokenUtils.getCurrentUser();

            // 1. 权限验证
            if (!currentUser.getId().equals(library.getUserId())) {
                return Result.error("403", "无操作权限");
            }

            // 2. 检查作品数量
            QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("library_id", id);
            long count = creationService.count(queryWrapper); // 使用 long 类型
            if (count > 0) {
                return Result.error("400", "请先删除图库内所有作品");
            }

            // 3. 执行删除
            return Result.success(libraryService.removeById(id));
        } catch (Exception e) {
            return Result.error("500", "删除失败：" + e.getMessage());
        }
    }

    // 获取所有图库
    @GetMapping
    public Result findAll() {
        return Result.success(libraryService.list()); // 返回所有信息
    }

    // 获取特定用户的所有
    @GetMapping("/user/{id}")
    public Result findUser(@PathVariable Integer id) {
        // 添加权限验证
        User currentUser = TokenUtils.getCurrentUser();
        if (!currentUser.getId().equals(id)) {
            return Result.error("403", "无权限访问");
        }

        LambdaQueryWrapper<Library> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Library::getUserId, id);
        return Result.success(libraryService.list(wrapper));
    }

    // 根据图书馆ID获取图库信息
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(libraryService.getById(id)); // 获取指定ID的图书馆信息
    }

    // 获取分页列表
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {

        QueryWrapper<Library> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }

        return Result.success(libraryService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    // 获取当前用户分页列表
    @GetMapping("/my/page")
    public Result findMyPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name) {

        QueryWrapper<Library> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.eq("user_id",TokenUtils.getCurrentUser().getId());

        return Result.success(libraryService.page(new Page<>(pageNum, pageSize), queryWrapper));

    }

    // 新增接口：获取图库内的作品数量
    @GetMapping("/{id}/creation-count")
    public Result getCreationCount(@PathVariable Integer id) {
        try {
            // 1. 权限验证：只能查询自己的图库
            Library library = libraryService.getById(id);
            User currentUser = TokenUtils.getCurrentUser();
            if (!currentUser.getId().equals(library.getUserId())) {
                return Result.error("403", "无权限访问");
            }

            // 2. 查询作品数量
            QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("library_id", id);
            long count = creationService.count(queryWrapper);

            return Result.success(count);
        } catch (Exception e) {
            return Result.error("500", "查询失败：" + e.getMessage());
        }
    }
    // 新增接口：获取图库内的作品列表（分页）
    @GetMapping("/{libraryId}/creations")
    public Result getLibraryCreations(
            @PathVariable Integer libraryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            // 1. 权限验证：确保用户有权访问该图库
//            Library library = libraryService.getById(libraryId);
//            User currentUser = TokenUtils.getCurrentUser();
//            if (!currentUser.getId().equals(library.getUserId())) {
//                return Result.error("403", "无权限访问");
//            }

            // 2. 查询作品列表（分页）
            QueryWrapper<Creation> wrapper = new QueryWrapper<>();
            wrapper.eq("library_id", libraryId);
            Page<Creation> pageData = creationService.page(new Page<>(page, size), wrapper);

            return Result.success(pageData);
        } catch (Exception e) {
            return Result.error("500", "查询失败：" + e.getMessage());
        }
    }
    // 在LibraryController中添加新接口
    @GetMapping("/{id}/creations/simple")
    public Result getSimpleCreations(@PathVariable Integer id) {
        QueryWrapper<Creation> wrapper = new QueryWrapper<>();
        wrapper.eq("library_id", id)
                .select("id", "img", "name") // 只返回必要字段
                .orderByDesc("id")
                .last("LIMIT 6");
        return Result.success(creationService.list(wrapper));
    }
    // 在LibraryController.java添加图库分页接口
    @GetMapping("/front/page")
    public Result frontPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "0") Integer typeId,
            @RequestParam(required = false) Integer userId) {

        // 1. 保持原有查询逻辑
        QueryWrapper<Library> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if (!"".equals(name)) {
            wrapper.like("name", name);
        }
        if (typeId != 0) {
            wrapper.eq("type_id", typeId);
        }
        if (userId != null) { // 添加用户过滤
            wrapper.eq("user_id", userId);
        }

        // 2. 执行分页查询
        Page<Library> page = libraryService.page(new Page<>(pageNum, pageSize), wrapper);

        // 3. 批量查询用户信息
        List<Integer> userIds = page.getRecords().stream()
                .map(Library::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4. 转换为 DTO
        List<LibraryDTO> dtoList = page.getRecords().stream().map(library -> {
            LibraryDTO dto = new LibraryDTO();
            BeanUtils.copyProperties(library, dto);  // 复制原字段
            User user = userMap.get(library.getUserId());
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // 5. 构建分页结果
        Page<LibraryDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(dtoList);
        return Result.success(dtoPage);
    }
}




