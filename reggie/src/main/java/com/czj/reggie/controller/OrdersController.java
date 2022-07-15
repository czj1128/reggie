package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.BaseContext;
import com.czj.reggie.common.R;
import com.czj.reggie.entity.Orders;
import com.czj.reggie.service.IOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private IOrdersService ordersService;

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("提交订单");
        ordersService.submit(orders);
        return R.success("提交订单");
    }

    /**
     * 订单查询（移动端）
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<Orders>> userPage(Integer page,Integer pageSize){
        log.info("移动端订单查询，page：{}，pageSize：{}",page,pageSize);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId, BaseContext.getCurrentId());
        ordersService.page(ordersPage,qw);
        return R.success(ordersPage);
    }

    /**
     * 订单查询（后台）
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> pageRage(Integer page,Integer pageSize){
        log.info("后台订单查询，page：{}，pageSize：{}",page,pageSize);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId, BaseContext.getCurrentId());
        ordersService.page(ordersPage,qw);
        return R.success(ordersPage);
    }

    /**
     * 派送订单
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> ps(@RequestBody Orders orders){
        log.info("订单派送");
        ordersService.updateById(orders);
        return R.success("订单派送");
    }
}
