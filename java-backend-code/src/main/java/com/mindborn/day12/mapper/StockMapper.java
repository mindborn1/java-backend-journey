package com.mindborn.day12.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindborn.day12.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}