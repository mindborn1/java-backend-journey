package com.mindborn.day12.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.day12.entity.Stock;
import com.mindborn.day12.mapper.StockMapper;
import com.mindborn.day12.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    /**
     * REQUIRED（默认）：加入调用方的事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deduct(Long productId, int count) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        Stock stock = stockMapper.selectOne(wrapper);

        if (stock == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }

        if (stock.getCount() < count) {
            throw new RuntimeException("库存不足，当前库存=" + stock.getCount() + "，需要=" + count);
        }

        stock.setCount(stock.getCount() - count);
        stockMapper.updateById(stock);

        System.out.println("库存扣减成功，商品=" + productId + "，剩余=" + stock.getCount());
    }

    /**
     * NESTED：在调用方事务内创建 savepoint
     */
    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void deductNested(Long productId, int count) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        Stock stock = stockMapper.selectOne(wrapper);

        if (stock == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }

        if (stock.getCount() < count) {
            throw new RuntimeException("库存不足，当前库存=" + stock.getCount() + "，需要=" + count);
        }

        stock.setCount(stock.getCount() - count);
        stockMapper.updateById(stock);

        System.out.println("NESTED 库存扣减成功，商品=" + productId + "，剩余=" + stock.getCount());
    }
}