package com.mindborn.day12.service;

/**
 * 库存服务接口
 */
public interface StockService {

    /**
     * REQUIRED：加入调用方事务
     */
    void deduct(Long productId, int count);

    /**
     * NESTED：在调用方事务内创建 savepoint
     */
    void deductNested(Long productId, int count);
}