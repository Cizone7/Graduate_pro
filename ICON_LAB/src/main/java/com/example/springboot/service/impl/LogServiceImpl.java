package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Log;
import com.example.springboot.mapper.LogMapper;
import com.example.springboot.service.ILogService;
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
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {


}
