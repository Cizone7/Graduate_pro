// CreationDTO.java
package com.example.springboot.controller.dto;

import com.example.springboot.entity.Creation;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreationDTO extends Creation {
    private String nickname;    // 作者昵称
    private String avatarUrl;   // 作者头像
}