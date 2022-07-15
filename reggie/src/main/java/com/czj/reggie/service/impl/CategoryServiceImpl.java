package com.czj.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czj.reggie.common.CustomException;
import com.czj.reggie.entity.Category;
import com.czj.reggie.entity.Dish;
import com.czj.reggie.entity.Setmeal;
import com.czj.reggie.mapper.CategoryMapper;
import com.czj.reggie.service.ICategoryService;
import com.czj.reggie.service.IDishService;
import com.czj.reggie.service.ISetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private IDishService dishService;//菜品
    @Autowired
    private ISetmealService setmealService;//套餐
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 删除分类，判断是否有关联的菜品或套餐
     * @param id
     */
    @Override
    public void removeByIdCZJ(Long id) {

        //1.查询当前分类是否关联了‘菜品‘  select * from dish where category_id=?
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id); //等价于SQL： where category_id=?
        int count = dishService.count(dishLambdaQueryWrapper);// 等价于SQL：select count(*) from dish where category_id=?
        if (count>0){
            //count数量大于0,表示已关联‘菜品’，无法删除成功，抛出业务异常
            throw new CustomException("当前分类信息关联了菜品，无法删除");
        }

        //2.查询当前分类是否关联了’套餐‘  select * from setmeal where category_id=?
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id); //等价于SQL： where category_id=?
        int count2 = setmealService.count(setmealLambdaQueryWrapper);// 等价于SQL：select count(*) from setmeal where category_id=?
        if (count2>0){
            //count2数量大于0,表示已关联‘套餐’，无法删除成功，抛出业务异常
            throw new CustomException("当前分类信息关联了套餐，无法删除");
        }

        //3.没有关联的菜品或套餐，执行删除
        categoryMapper.deleteById(id);
    }
}
