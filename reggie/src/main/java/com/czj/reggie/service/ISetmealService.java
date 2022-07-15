package com.czj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czj.reggie.dto.DishDto;
import com.czj.reggie.dto.SetmealDto;
import com.czj.reggie.entity.Setmeal;

import java.util.List;

public interface ISetmealService extends IService<Setmeal> {


    public void saveCZJ(SetmealDto setmealDto);

    public void removeCZJ(List<Long> ids);

    public SetmealDto getByIdCZJ(Long id);

    public void updateCZJ(SetmealDto setmealDto);

}
