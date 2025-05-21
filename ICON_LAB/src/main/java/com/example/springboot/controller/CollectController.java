package com.example.springboot.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICollectService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collecteService;

     // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Collect collect) {

        try{
            collect.setUserId(TokenUtils.getCurrentUser().getId());
            collecteService.save(collect);
        }catch (Exception e){
            return Result.error("685","您已经收藏过该作品");

        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,TokenUtils.getCurrentUser().getId());
        wrapper.eq(Collect::getItemId,id);
        collecteService.remove(wrapper);
        return Result.success();
    }

}

