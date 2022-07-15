package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.R;
import com.czj.reggie.entity.Category;
import com.czj.reggie.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增category：{}",category);
        categoryService.save(category);
        return R.success("分类添加成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize){
        log.info("Category分页查询 page：{}，pageSize：{}",page,pageSize);
        //构造分页构造器
        Page<Category> catePage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //按照排序字段‘升序’排序
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(catePage,queryWrapper);
        return R.success(catePage);
    }

    /**
     * 分类信息删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> removeById(Long ids){
        log.info("根据category的id删除，id：{}",ids);
        categoryService.removeByIdCZJ(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改category:{}",category);
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }

    /**
     * 根据条件查询分类类型（type=1、菜品分类，2、套餐分类）的所对应的分类信息（
     *      如type=1,分类类型为菜品分类，查询所有菜品分类 如：川菜、冒菜、热菜、凉菜等
     * ）
     * @param category
     * @return
     */
    @GetMapping("list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据category表字段type字段查询 type=1（菜品分类） type=2（套餐分类）
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //进行数据排序 ，按照排序（sort）字段’升序‘，sort相同，按照修改时间’降序‘排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询满足条件的数据
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
