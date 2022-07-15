package com.czj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czj.reggie.dto.DishDto;
import com.czj.reggie.entity.Dish;

import java.util.List;

public interface IDishService extends IService<Dish> {

    public void saveCZJ(DishDto dishDto);

    public DishDto getByIdCZJ(Long id);

    public void updateCZJ(DishDto dishDto);

    public void removeByIdsCZJ(Long[] ids);

    public void updateStatus(int status, List<Long> ids);

}
