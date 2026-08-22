package com.mindborn.day12;

import com.mindborn.day12.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 事务传播行为测试类
 *
 * 运行方式：右键 → Run 'TransactionPropagationTest.testXxx'
 * 每次只运行一个测试方法，观察数据库结果
 *
 * 验证 SQL（每次测试后在 MySQL 客户端执行）：
 *   SELECT * FROM t_order;
 *   SELECT * FROM t_stock;
 *   SELECT * FROM t_operation_log;
 */
@SpringBootTest
public class TransactionPropagationTest {

    @Autowired
    private OrderService orderService;

    /**
     * 场景1：REQUIRED 正常下单
     * 输入：商品1（库存100），买1个
     * 预期：t_order 多一条，t_stock 商品1变成99
     */
    @Test
    public void testRequiredSuccess() {
        System.out.println("\n========== 场景1：REQUIRED 正常下单 ==========");
        orderService.placeOrderSuccess(1L, 1L, 1);
        System.out.println("========== 场景1 结束 ==========\n");
    }

    /**
     * 场景2：REQUIRED 库存不足，全部回滚
     * 输入：商品2（库存0），买1个
     * 预期：t_order 无新增（回滚了），t_stock 商品2仍为0
     */
    @Test
    public void testRequiredFail() {
        System.out.println("\n========== 场景2：REQUIRED 库存不足，全部回滚 ==========");
        try {
            orderService.placeOrderFail(1L, 2L, 1);
        } catch (Exception e) {
            System.out.println("捕获异常：" + e.getMessage());
        }
        System.out.println("========== 场景2 结束 ==========\n");
    }

    /**
     * 场景3：REQUIRES_NEW 日志保留
     * 输入：商品2（库存0），买1个
     * 预期：t_order 无新增（回滚），t_stock 不变，t_operation_log 有一条！
     */
    @Test
    public void testRequiresNew() {
        System.out.println("\n========== 场景3：REQUIRES_NEW 日志保留 ==========");
        try {
            orderService.placeOrderWithLog(1L, 2L, 1);
        } catch (Exception e) {
            System.out.println("捕获异常：" + e.getMessage());
        }
        System.out.println("========== 场景3 结束 ==========\n");
    }

    /**
     * 场景4：NESTED 部分回滚
     * 输入：商品2（库存0），买1个
     * 预期：t_order 有一条（订单保留了），t_stock 商品2仍为0（库存回滚了）
     */
    @Test
    public void testNested() {
        System.out.println("\n========== 场景4：NESTED 部分回滚 ==========");
        orderService.placeOrderWithNested(1L, 2L, 1);
        System.out.println("========== 场景4 结束 ==========\n");
    }
}