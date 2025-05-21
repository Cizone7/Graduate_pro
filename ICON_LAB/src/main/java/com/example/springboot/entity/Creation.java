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
@TableName(value = "creation")
public class Creation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Alias("作品标题")
    private String name;

    @Alias("作品分类")
    private Integer typeId;

    @Alias("创作类型")
    private String style;

    @Alias("上传用户id")
    private Integer userId;

    @Alias("上传时间")
    private String time;

    @Alias("作品文件")
    private String img;

    @Alias("作品积分")
    private Integer count;

    @Alias("作品库ID")
    private Integer libraryId;

}
