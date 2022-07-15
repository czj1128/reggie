package com.czj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czj.reggie.entity.Orders;
import org.springframework.core.annotation.Order;

public interface IOrdersService extends IService<Orders> {
    public void submit(Orders order);

}
