package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Rate;
import com.example.springboot.mapper.RateMapper;
import com.example.springboot.service.IRateService;
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
public class RateServiceImpl extends ServiceImpl<RateMapper, Rate> implements IRateService {


}
