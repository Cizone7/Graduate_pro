package com.example.springboot.controller.dto;

import lombok.Data;

/**
 * 接受前端修改密码请求的参数
 */

@Data
public class UserPasswordDTO {
    private String username;
    private String phone;
    private String password;
    private String newPassword;
}
