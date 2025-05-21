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
@TableName(value = "library")
public class Library implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Alias("库名")
    private String name;

    @Alias("上传用户ID")
    private Integer userId;

    @Alias("上传时间")
    private String time;

    @Alias("作品数量")
    private Integer creationCount;


}
