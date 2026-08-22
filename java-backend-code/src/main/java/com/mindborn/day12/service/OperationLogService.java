package com.mindborn.day12.service;

/**
 * 日志服务接口
 */
public interface OperationLogService {

    /**
     * REQUIRES_NEW：独立事务，必须记录
     */
    void record(String operation);
}