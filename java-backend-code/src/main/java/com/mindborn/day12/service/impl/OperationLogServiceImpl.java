package com.mindborn.day12.service.impl;

import com.mindborn.day12.entity.OperationLog;
import com.mindborn.day12.mapper.OperationLogMapper;
import com.mindborn.day12.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper logMapper;

    /**
     * REQUIRES_NEW：挂起当前事务，新建一个完全独立的事务
     *
     * 使用场景：审计日志、监控打点——不管业务成功失败，必须留下记录
     * 代价：多一次事务提交，性能略低，但在"必须记录"的场景下值得
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String operation) {
        OperationLog log = new OperationLog();
        log.setOperation(operation);
        logMapper.insert(log);
        System.out.println("日志记录成功，内容=" + operation);
    }
}