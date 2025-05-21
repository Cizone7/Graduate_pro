package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Library;
import com.example.springboot.mapper.LibraryMapper;
import com.example.springboot.service.ILibraryService;
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
public class LibraryServiceImpl extends ServiceImpl<LibraryMapper, Library> implements ILibraryService {


}
