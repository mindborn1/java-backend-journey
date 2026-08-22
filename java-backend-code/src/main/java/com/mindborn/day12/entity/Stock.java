package com.mindborn.day12.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库存实体
 * 对应 t_stock 表
 */
@Data
@TableName("t_stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Integer count;
}