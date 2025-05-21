package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Creation;
import com.example.springboot.entity.Type;
import com.example.springboot.mapper.CreationMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.service.ICreationService;
import com.example.springboot.service.ITypeService;
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
public class CreationServiceImpl extends ServiceImpl<CreationMapper, Creation> implements ICreationService {


}
