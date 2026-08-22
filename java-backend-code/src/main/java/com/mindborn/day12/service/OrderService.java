package com.mindborn.day12.service;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 场景1：REQUIRED 正常下单
     */
    void placeOrderSuccess(Long userId, Long productId, int count);

    /**
     * 场景2：REQUIRED 库存不足，全部回滚
     */
    void placeOrderFail(Long userId, Long productId, int count);

    /**
     * 场景3：REQUIRES_NEW 日志保留
     */
    void placeOrderWithLog(Long userId, Long productId, int count);

    /**
     * 场景4：NESTED 部分回滚
     */
    void placeOrderWithNested(Long userId, Long productId, int count);
}