package com.example.springboot.controller;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.Rate;
import com.example.springboot.entity.Record;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.ILogService;
import com.example.springboot.service.IRateService;
import com.example.springboot.service.IRecordService;
import com.example.springboot.service.IUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
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
@RequestMapping("/record")
public class RecordController {

    @Resource
    private IRecordService recordService;

    @Resource
    private IUserService userService;

    @Resource
    private IRateService rateService;

    @Resource
    private ILogService logService;

     // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Record record) {

        return Result.success(recordService.saveOrUpdate(record));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(recordService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(recordService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(recordService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(recordService.getById(id));
    }

    // 将原doRecharge拆分为两个方法
    @Transactional
    public void doRechargeCallback(Record record) {
        // 2. 更新用户积分
        Rate rate = rateService.list().stream()
                .filter(r -> r.getAmount().compareTo(record.getAmount()) == 0)
                .findFirst()
                .orElseThrow(() -> new ServiceException(Constants.CODE_400, "无效的充值金额"));

        userService.updateCount(record.getUserId(), rate.getPoints());

        // 3. 记录日志（示例，需补充日志参数）
        Log log = new Log();
        log.setUserId(record.getUserId());
        log.setOperationType("RECHARGE");
        logService.save(log);
    }

    // 原doRecharge方法修改为创建订单
    @PostMapping("/recharge")
    public Result createRechargeOrder(@RequestBody Record record) {
        List<Rate> rates = rateService.list();
        Rate rate = rates.stream()
                .filter(r -> r.getAmount().compareTo(record.getAmount()) == 0)
                .findFirst()
                .orElseThrow(() -> new ServiceException(Constants.CODE_400, "无效的充值金额"));

        // 生成唯一订单号（示例：使用Hutool的Snowflake算法）
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        record.setOrderNo(orderNo); // 关键：设置订单号
        // 创建待支付订单
        record.setPoints(rate.getPoints());
        record.setStatus("WAIT"); // 初始状态为等待支付
        recordService.save(record);

        return Result.success(record.getOrderNo()); // 返回订单号用于支付
    }

    @GetMapping("/records/{userId}")
    public Result getRecords(
            @PathVariable Integer userId,
            @RequestParam Integer pageNum,  // 接收分页参数
            @RequestParam Integer pageSize) {

        // 使用 MyBatis-Plus 分页查询
        Page<Record> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        // 返回分页结果（Page 对象包含 records 和 total 字段）
        return Result.success(recordService.page(page, queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String amount,
                           @RequestParam(defaultValue = "") String nickname) {

        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        if (!"".equals(amount)) {
            queryWrapper.eq("amount", new BigDecimal(amount));
        }
        // 按用户昵称查询（联表查询逻辑）
        if (!"".equals(nickname)) {
            // 1. 查询匹配昵称的用户ID列表
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("nickname", nickname);
            List<Integer> userIds = userService.list(userQueryWrapper)
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            // 2. 根据用户ID列表过滤记录
            if (!userIds.isEmpty()) {
                queryWrapper.in("user_id", userIds);
            } else {
                // 无匹配用户时返回空数据
                return Result.success(new Page<>(pageNum, pageSize, 0));
            }
        }
        return Result.success(recordService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

