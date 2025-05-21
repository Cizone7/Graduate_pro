package com.example.springboot.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Rate;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IRateService;
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
@RequestMapping("/rate")
public class RateController {

    @Resource
    private IRateService rateService;

     // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Rate rate) {

        return Result.success(rateService.saveOrUpdate(rate));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(rateService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(rateService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(rateService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(rateService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String amount) {

        QueryWrapper<Rate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(amount)) {
            queryWrapper.like("amount", amount);
        }

        return Result.success(rateService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

