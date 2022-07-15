package com.czj.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czj.reggie.entity.DishFlavor;
import com.czj.reggie.mapper.DishFlavorMapper;
import com.czj.reggie.service.IDishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements IDishFlavorService {
}
