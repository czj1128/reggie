package com.czj.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czj.reggie.common.CustomException;
import com.czj.reggie.dto.DishDto;
import com.czj.reggie.dto.SetmealDto;
import com.czj.reggie.entity.Setmeal;
import com.czj.reggie.entity.SetmealDish;
import com.czj.reggie.mapper.SetmealMapper;
import com.czj.reggie.service.ISetmealDishService;
import com.czj.reggie.service.ISetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements ISetmealService {

    @Autowired
    private ISetmealDishService setmealDishService;

    /**
     * 添加套餐setmeal，保存套餐菜品关系setmeal_dish
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveCZJ(SetmealDto setmealDto) {
        //添加套餐
        this.save(setmealDto);
        //添加套餐和菜品关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((item)->{
            //给setmeal_dish表 关联 setmeal表 id
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //执行添加
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 批量删除套餐，删除setmeal表基本信息，和setmeal_dish表套餐菜品关系
     * @param ids
     */
    @Override
    @Transactional
    public void removeCZJ(List<Long> ids) {
        //查询套餐是否是起售状态
        LambdaQueryWrapper<Setmeal> setmealqueryWrapper = new LambdaQueryWrapper<>();//where
        setmealqueryWrapper.in(Setmeal::getId,ids);//id in(?,?,?,...)
        setmealqueryWrapper.eq(Setmeal::getStatus,1);//status=1
        int count = this.count(setmealqueryWrapper);//select count(*) from setmeal where id status=1 and in(?,...)
        //判断需要删除的数据有几条在售卖中，大于0条则抛出异常
        if (count>0){
            throw new CustomException("套餐售卖中...无法删除");
        }
        //删除套餐表id对应的基本信息
        this.removeByIds(ids);
        //删除套餐菜品关系表，套餐id对应的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();//where
        queryWrapper.in(SetmealDish::getSetmealId,ids); //setmeal_id in(?,?,?,...)
        setmealDishService.remove(queryWrapper);//delete from setmeal_dish where setmeal_id in(?,?,?,...)
    }

    /**
     * 根据id查询套餐及套餐菜品关系表的信息，并封装到DTO对象
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdCZJ(Long id) {
        //获得套餐基本信息
        Setmeal setmeal = this.getById(id);
        //获取套餐菜品关系表信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        //创建DTO对象，将setmeal值拷贝给setmealDto
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }


    /**
     * 修改套餐信息和套餐菜品关系表信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateCZJ(SetmealDto setmealDto) {
        //修改套餐基本信息
        this.updateById(setmealDto);
        //修改套餐菜品关系表信息,通过删除原数据，添加新数据实现
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();// from setmeal_dish where
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId()); //setmeal_id=?
        setmealDishService.remove(queryWrapper);//delete from setmeal_dish where setmeal_id=?
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((item)->{
            //套餐菜品关系表添加的新数添加套餐id
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
