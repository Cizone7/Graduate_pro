package com.example.springboot.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author
 * 
 */

@Data
@TableName(value = "sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Alias("用户名")
    private String username;

    @Alias("密码")
    private String password;

    @Alias("昵称")
    private String nickname;

    @Alias("邮箱")
    private String email;

    @Alias("电话")
    private String phone;

    @Alias("地址")
    private String address;

    @Alias("头像")
    private String avatarUrl;

    @Alias("角色")
    private String role;

    @Alias("积分")
    private Integer count;

    @Alias("简介")
    private String info;


}
