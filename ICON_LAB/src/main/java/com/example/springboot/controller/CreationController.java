package com.example.springboot.controller;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.controller.dto.CreationDTO;
import com.example.springboot.entity.*;
import com.example.springboot.service.*;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
@RequestMapping("/creation")
public class CreationController {

    @Resource
    private ICreationService creationService;
    @Resource
    private ICollectService collectService;

    @Resource
    private IUserService userService;

    @Resource
    private ITypeService typeService;

    @Resource
    private ILibraryService libraryService;

    @Resource
    private ILogService logService;

    @Resource
    private IDownloadService downloadService;


    @GetMapping("/count1")
    public Result count1(){

        List<Type> list = typeService.list();

        Map<Integer, Long> map = creationService.list().stream().collect(Collectors.groupingBy(Creation::getTypeId,Collectors.counting()));

        JSONArray array = new JSONArray();

        for (Type type : list) {
            JSONObject object = new JSONObject();
            object.set("name",type.getName());
            object.set("value",map.getOrDefault(type.getId(),0L));
            array.add(object);
        }

        return Result.success(array);
    }

    @GetMapping("/count2")
    public Result count2(){

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCount);
        wrapper.eq(User::getRole,"ROLE_USER");
        wrapper.last("LIMIT 5");
        List<User> topUsers = userService.list(wrapper);

        JSONArray array = new JSONArray();

        for (User user : topUsers) {
            JSONObject object = new JSONObject();
            object.set("name",user.getNickname());
            object.set("value",user.getCount());
            array.add(object);
        }

        return Result.success(array);
    }

     // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Creation creation) {
        User currentUser = TokenUtils.getCurrentUser();
        if(creation.getId() == null){
            creation.setUserId(currentUser.getId());
            creation.setTime(DateUtil.now());

            //拿到当前用户的积分
            currentUser.setCount( currentUser.getCount()+10);
           userService.updateById(currentUser);

        }
        boolean success = creationService.saveOrUpdate(creation);
        if (!success) {
            throw new RuntimeException("作品保存失败");
        }

        return Result.success(creationService.saveOrUpdate(creation));
    }

    // 多图上传
    @PostMapping("/batch")
    public Result saveBatch(@RequestBody List<Creation> creations, HttpServletRequest request) {
        User currentUser = TokenUtils.getCurrentUser();

        Integer libraryId = creations.get(0).getLibraryId();
        int newCount = 0;
        for (Creation creation : creations) {
            if (creation.getId() == null) {
                creation.setUserId(currentUser.getId());
                creation.setTime(DateUtil.now());
                newCount++;
            }
        }

        creationService.saveOrUpdateBatch(creations);

        // 4. 更新图库作品数量（与用户积分更新模式相同）
        if (newCount > 0) {
            Library library = libraryService.getById(libraryId);
            library.setCreationCount(library.getCreationCount() + newCount); // 直接操作字段
            libraryService.updateById(library); // 单条更新
        }

        // 5. 更新用户积分（原写法保持不变）
        currentUser.setCount(currentUser.getCount() + 10 * creations.size());
        userService.updateById(currentUser);

        Log log = new Log();
        log.setUserId(currentUser.getId());
        log.setOperationType("UPLOAD");
        log.setOperationDetail("用户上传"+creations.size()+"作品积分：+"+10 * creations.size() );
        log.setIp(request.getRemoteAddr());
        logService.save(log);

        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            Creation creation = creationService.getById(id);
            if (creation == null) {
                return Result.error("404", "作品不存在");
            }

            // 记录关联的图库ID
            Integer libraryId = creation.getLibraryId();

            // 删除作品
            boolean success = creationService.removeById(id);
            if (!success) {
                throw new RuntimeException("删除失败");
            }

            // 更新图库数量（直接操作 Integer 字段）
            if (libraryId != null) {
                Library library = libraryService.getById(libraryId);
                if (library != null) {
                    library.setCreationCount(library.getCreationCount() - 1); // Integer 类型操作
                    libraryService.updateById(library);
                }
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("500", "删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(creationService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(creationService.list());
    }

    @GetMapping("/user/{id}")
    public Result findUser(@PathVariable Integer id) {
        LambdaQueryWrapper<Creation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Creation::getUserId,id);
        return Result.success(creationService.list(wrapper));
    }

    @GetMapping("/userAddCount/{id}/creat/{count}/item/{item}")
    public Result userAddCount(@PathVariable Integer id,
                               @PathVariable Integer count,
                               @PathVariable Integer item,
                               HttpServletRequest request) {

        try {
            User author = userService.getById(id);
            User currentUser = TokenUtils.getCurrentUser();

            if(currentUser == null) {
                return Result.error("401", "请先登录");
            }
            //检查用户是否已下载过该作品
            boolean exists = downloadService.lambdaQuery()
                    .eq(Download::getUserId, currentUser.getId())
                    .eq(Download::getItemId, item)
                    .exists();
            if (exists) {
                return Result.success("已下载过此作品，本次下载不消耗积分");
            }
            if(currentUser.getCount() < count) {
                return Result.error("402", "积分不足");
            }
            // 更新作者积分
            author.setCount(author.getCount() + count);
            userService.updateById(author);

            Log log1 = new Log();
            log1.setUserId(author.getId());
            log1.setOperationType("BE_DOWNLOADED");
            log1.setOperationDetail("作者作品："+item+"，积分：+"+ count);
            log1.setIp(request.getRemoteAddr());
            logService.save(log1);

            // 扣除当前用户积分
            currentUser.setCount(currentUser.getCount() - count);
            userService.updateById(currentUser);

            Download record = new Download();
            record.setUserId(currentUser.getId());
            record.setItemId(item);
            record.setDownloadTime(new Date());
            downloadService.save(record);


            Log log = new Log();
            log.setUserId(currentUser.getId());
            log.setOperationType("DOWNLOAD");
            log.setOperationDetail("用户下载作品id："+item+"，积分：-"+ count);
            log.setIp(request.getRemoteAddr());
            logService.save(log);
            return Result.success();
        } catch (Exception e) {
            return Result.error("500", "系统错误：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(creationService.getById(id));
    }


    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {

        QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }

        return Result.success(creationService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/my/page")
    public Result findMyPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {

        QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.eq("user_id",TokenUtils.getCurrentUser().getId());

        return Result.success(creationService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/collect/page")
    public Result findCollectPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name) {

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,TokenUtils.getCurrentUser().getId());
        List<Integer> ids = collectService.list(wrapper).stream().map(Collect::getItemId).collect(Collectors.toList());

        if(CollectionUtil.isEmpty(ids))return Result.success(collectService.page(new Page<>(pageNum, pageSize), wrapper));

        QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.in("id",ids);

        return Result.success(creationService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/front/page")
    public Result findPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) String style,
            @RequestParam(defaultValue = "") String name) {

        // 1. 查询作品分页数据
        Page<Creation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Creation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", "%" + name + "%");
        }
        if (typeId != null && typeId != 0) {
            queryWrapper.eq("type_id", typeId);
        }
        if (StringUtils.hasText(style)) {  // 新增style筛选条件
            queryWrapper.eq("style", style);
        }
        Page<Creation> creationPage = creationService.page(page, queryWrapper);

        // 2. 批量查询用户信息
        List<Integer> userIds = creationPage.getRecords().stream()
                .map(Creation::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. 转换为CreationDTO
        List<CreationDTO> dtoList = creationPage.getRecords().stream().map(creation -> {
            CreationDTO dto = new CreationDTO();
            BeanUtils.copyProperties(creation, dto);  // 复制公共字段
            User user = userMap.get(creation.getUserId());
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // 4. 构建分页结果
        Page<CreationDTO> dtoPage = new Page<>(creationPage.getCurrent(), creationPage.getSize(), creationPage.getTotal());
        dtoPage.setRecords(dtoList);
        return Result.success(dtoPage);
    }

}

