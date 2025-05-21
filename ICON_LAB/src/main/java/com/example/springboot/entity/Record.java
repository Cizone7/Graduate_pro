package com.example.springboot.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author
 * 
 */

@Data
@TableName(value = "record")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String orderNo;
    private BigDecimal amount;
    private Integer points;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private String Status;
    private String outTradeNo; // 新增：支付宝订单号
    private Date payTime;


}
