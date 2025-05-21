package com.example.springboot.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author
 * 
 */

@Data
@TableName(value = "collect")
public class Collect implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer userId;
    private Integer itemId;


}
