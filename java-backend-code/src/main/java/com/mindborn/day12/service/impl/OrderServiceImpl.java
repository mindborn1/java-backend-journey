package com.mindborn.day12.service.impl;

import com.mindborn.day12.entity.Order;
import com.mindborn.day12.mapper.OrderMapper;
import com.mindborn.day12.service.OrderService;
import com.mindborn.day12.service.StockService;
import com.mindborn.day12.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 订单服务实现类
 *
 * 核心目标：通过4个场景，直观感受 REQUIRED / REQUIRES_NEW / NESTED 的区别
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 场景1：全部 REQUIRED，正常下单
     *
     * 传播链：placeOrderSuccess(REQUIRED) → stockService.deduct(REQUIRED) 【同一个事务】
     * 预期：订单创建成功，库存从100变成99
     */
    @Override
    @Transactional
    public void placeOrderSuccess(Long userId, Long productId, int count) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        stockService.deduct(productId, count);

        System.out.println("【场景1】下单成功，订单ID=" + order.getId());
    }

    /**
     * 场景2：全部 REQUIRED，库存不足 → 全部回滚
     *
     * 传播链：placeOrderFail(REQUIRED) → stockService.deduct(REQUIRED) 【抛异常，整体回滚】
     * 预期：订单回滚（数据库无新增），库存不变（仍为0）
     */
    @Override
    @Transactional
    public void placeOrderFail(Long userId, Long productId, int count) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        System.out.println("【场景2】订单已创建（未提交），订单ID=" + order.getId());

        // 扣库存会抛"库存不足"异常，导致当前事务回滚
        stockService.deduct(productId, count);

        System.out.println("【场景2】这行不会打印");
    }


    /**
     * 场景3：REQUIRES_NEW 演示
     *
     * 传播链：
     *   placeOrderWithLog(REQUIRED)
     *     → logService.record(REQUIRES_NEW)  【挂起当前事务，新建独立事务，立即提交】
     *     → stockService.deduct(REQUIRED)      【加入当前事务，抛异常】
     *
     * 预期：订单回滚，但日志保留！
     * 关键：REQUIRES_NEW 会挂起外部事务，自己开一个全新事务并立即提交。
     *      外部事务后续回滚，不影响这个已经提交的新事务。
     */
    @Override
    @Transactional
    public void placeOrderWithLog(Long userId, Long productId, int count) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        // 先记录日志（REQUIRES_NEW，独立事务，立即提交！）
        // 即使后面抛异常，这条日志也已经写进数据库了
        operationLogService.record("【场景3】用户" + userId + "尝试下单，商品=" + productId);
        System.out.println("【场景3】日志已记录（独立事务已提交）");

        // 扣库存（会抛异常，当前事务回滚）
        stockService.deduct(productId, count);
    }

    /**
     * 场景4：NESTED 演示
     *
     * 传播链：
     *   placeOrderWithNested(REQUIRED)
     *     → stockService.deductNested(NESTED)  【创建 savepoint】
     *
     * 预期：订单保留（异常被 catch），库存不变（回滚到 savepoint）
     *
     * 关键理解：NESTED 不是独立事务，而是在当前事务里打一个"存档点"（savepoint）。
     * 内部失败 → 回滚到 savepoint → 外部事务可以继续执行。
     * 就像玩游戏：存档 → 打BOSS失败 → 读档 → 继续游戏。
     */
    @Override
    @Transactional
    public void placeOrderWithNested(Long userId, Long productId, int count) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        System.out.println("【场景4】订单已创建，订单ID=" + order.getId());

        try {
            stockService.deductNested(productId, count);
            System.out.println("【场景4】库存扣减成功");
        } catch (Exception e) {
            // 捕获异常，只回滚 savepoint，订单仍然保留！
            System.out.println("【场景4】库存扣减失败，但订单保留！异常：" + e.getMessage());
        }

        System.out.println("【场景4】订单最终状态：已创建（未被回滚）");
    }
}
