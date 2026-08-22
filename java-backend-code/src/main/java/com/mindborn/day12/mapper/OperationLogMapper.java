package com.mindborn.day12.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindborn.day12.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}