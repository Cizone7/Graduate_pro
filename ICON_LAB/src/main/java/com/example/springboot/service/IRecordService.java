package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Record;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * 
 */
public interface IRecordService extends IService<Record> {
    Record selectByOrderno(String orderNo);

}

