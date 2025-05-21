package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Record;
import com.example.springboot.mapper.RecordMapper;
import com.example.springboot.service.IRecordService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * 
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements IRecordService {

    @Override
    public Record selectByOrderno(String orderNo) {
        return getOne(new QueryWrapper<Record>().eq("order_no", orderNo));
    }
}
