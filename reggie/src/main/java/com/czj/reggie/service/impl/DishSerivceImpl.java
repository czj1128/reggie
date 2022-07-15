package com.czj.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czj.reggie.common.CustomException;
import com.czj.reggie.dto.DishDto;
import com.czj.reggie.entity.Dish;
import com.czj.reggie.entity.DishFlavor;
import com.czj.reggie.entity.SetmealDish;
import com.czj.reggie.mapper.DishMapper;
import com.czj.reggie.service.IDishFlavorService;
import com.czj.reggie.service.IDishService;
import com.czj.reggie.service.ISetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishSerivceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService {
    @Autowired
    private IDishFlavorService dishFlavorService;
    @Autowired
    private ISetmealDishService setmealDishService;

    /**
     * 添加菜品，并且保存菜品口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveCZJ(DishDto dishDto) {
        //保存菜品基本信息到dish表
        this.save(dishDto);
        //获取菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        Long dishId = dishDto.getId();
        //将表菜品表id和菜品口味表相对应
        flavors=flavors.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        //保存菜品口味到dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }


    /**
     * 根据id查询菜品及菜品口味的信息，并封装到DTO对象
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdCZJ(Long id) {
        DishDto dishDto = new DishDto();
        //查询菜品基本信息
        Dish dish = this.getById(id);
        //将菜品信息拷贝到dishDto对象
        BeanUtils.copyProperties(dish,dishDto);
        //查询菜品id对应的菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //添加菜品口味到dishDto对象
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 修改菜品及菜品口味表信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateCZJ(DishDto dishDto) {
        //修改菜品表信息
        this.updateById(dishDto);
        //修改菜品口味表信息,通过先删除原来数据，再添加新数据方式实现
        //删除菜品表id对应的菜品口味表信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加菜品id对应的菜品口味表信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            //对菜品口味表添加菜品表id
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 批量删除菜品及菜品口味表信息
     * @param ids
     */
    @Override
    @Transactional
    public void removeByIdsCZJ(Long[] ids) {
        //查询菜品是否在启售状态
        LambdaQueryWrapper<Dish> dishquer = new LambdaQueryWrapper<>();
        dishquer.in(Dish::getId,ids);// id in(?,...)
        dishquer.eq(Dish::getStatus,1); //status=1
        int count = this.count(dishquer);//select count(*) from dish where status=1 and id in(?,...)
        if (count>0){
            throw new CustomException("菜品售卖中，无法删除");
        }
        //查询菜品是否关联了套餐
        LambdaQueryWrapper<SetmealDish> sdQueryWrapper = new LambdaQueryWrapper<>();
        sdQueryWrapper.in(SetmealDish::getDishId,ids);//dish_id in(?,...)
        int sdCount = setmealDishService.count(sdQueryWrapper);
        if (sdCount>0){
            throw new CustomException("菜品关联了套餐，无法删除");
        }
        //删除菜品基本信息
        this.removeByIds(Arrays.asList(ids));
        //删除菜品id对应的菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            queryWrapper.eq(DishFlavor::getDishId,id); //where dish_id=?
            dishFlavorService.remove(queryWrapper);//delete from dish_flavor where dish_id=?
        }
    }

    /**
     * 批量修改菜品售卖状态
     * @param status
     * @param ids
     */
    @Override
    @Transactional
    public void updateStatus(int status, List<Long> ids) {
        //查询菜品是否关联了套餐
        LambdaQueryWrapper<SetmealDish> sdQueryWrapper = new LambdaQueryWrapper<>();
        sdQueryWrapper.in(SetmealDish::getDishId,ids);//dish_id in(?,...)
        int sdCount = setmealDishService.count(sdQueryWrapper);
        if (sdCount>0){
            throw new CustomException("菜品关联了套餐，无法修改售卖状态");
        }
        //修改菜品售卖状态
        List<Dish> dishes = ids.stream().map((id) -> {
            Dish dish = new Dish();
            //获得菜品id
            dish.setId(id);
            //修改菜品售卖状态
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        this.updateBatchById(dishes);
    }
}
