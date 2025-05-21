// LibraryDTO.java
package com.example.springboot.controller.dto;

import com.example.springboot.entity.Library;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LibraryDTO extends Library {
    private String nickname;    // 作者昵称
    private String avatarUrl;   // 作者头像
}