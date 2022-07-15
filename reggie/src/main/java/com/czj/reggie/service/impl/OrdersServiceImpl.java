package com.czj.reggie.service.impl;

import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czj.reggie.common.BaseContext;
import com.czj.reggie.common.CustomException;
import com.czj.reggie.entity.*;
import com.czj.reggie.mapper.OrdersMapper;
import com.czj.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private IUserService userService;
    @Autowired
    private IAddressBookService addressBookService;
    @Autowired
    private IShoppingCartService shoppingCartService;
    @Autowired
    private IOrderDetailService orderDetailService;


    /**
     * 提交订单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> ShoppingCartqueryWrapper = new LambdaQueryWrapper<>();
        ShoppingCartqueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(ShoppingCartqueryWrapper);
        if (shoppingCartList==null){
            throw new CustomException("购物车为空,无法下单");
        }

        //获取用户数据
        User user = userService.getById(userId);

        //获取下单地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook==null){
            throw new CustomException("地址为空,无法下单");
        }

        //添加订单表数据

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);//用于计算总金额

        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);
        //添加订单明细表数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车
        /*LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());*/
        shoppingCartService.remove(ShoppingCartqueryWrapper);
    }
}
