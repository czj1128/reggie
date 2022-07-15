package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.R;
import com.czj.reggie.dto.SetmealDto;
import com.czj.reggie.entity.Category;
import com.czj.reggie.entity.Dish;
import com.czj.reggie.entity.Setmeal;
import com.czj.reggie.entity.SetmealDish;
import com.czj.reggie.service.ICategoryService;
import com.czj.reggie.service.ISetmealDishService;
import com.czj.reggie.service.ISetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private ISetmealService setmealService;
    @Autowired
    private ISetmealDishService setmealDishService;
    @Autowired
    private ICategoryService categoryService;


    /**
     * 添加套餐信息
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("添加套餐，套餐内容:{}",setmealDto);
        setmealService.saveCZJ(setmealDto);
        return R.success("保存成功");
    }

    /**
     *套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        log.info("套餐分页，page：{}，pagesize：{}，name：{}",page,pageSize,name);
        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //模糊查询，根据套餐名称查询套餐
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页
        setmealService.page(setmealPage,queryWrapper);
        //对象拷贝  可排除需要拷贝的属性值 如：setmealDtoPage对象中的records属性值
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //获取套餐基本内容
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //获得当前套餐所在分类
            Category category = categoryService.getById(setmealDto.getCategoryId());
            //获取当前分类名称
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        //将拥有了分类名称的封装对象赋值给setmealDtoPage
        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> remove(@RequestParam List<Long> ids){
        log.info("删除套餐id：{}",ids);
        setmealService.removeCZJ(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 批量修改套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("修改套餐售卖状态，status：{}，ids：{}",status,ids);
        List<Setmeal> setmeals = ids.stream().map((id) -> {
            Setmeal setmeal = new Setmeal();
            //获得套餐id
            setmeal.setId(id);
            //修改套餐售卖状态
            setmeal.setStatus(status);
            return setmeal;
        }).collect(Collectors.toList());
        setmealService.updateBatchById(setmeals);
        return R.success("修改成功");
    }

    /**
     * 根据id查询套餐信息和套餐菜品关系表信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Cacheable(value = "setmealCache",key = "setmeal+'_'+#id")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("套餐信息，id：{}",id);
        SetmealDto setmealDto = setmealService.getByIdCZJ(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("套餐修改信息：{}",setmealDto);
        setmealService.updateCZJ(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 根据分类id查询对应套餐类型中的所有套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("套餐类型id：{}",setmeal.getCategoryId());

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //查询当前套餐分类中所有套餐
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());// where category_id=?
        //只查询起售状态（status=1）的菜品
        queryWrapper.eq(Setmeal::getStatus,1); // where status=1
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);//order by update_time desc
        //执行查询
        List<Setmeal> setmealList = setmealService.list(queryWrapper);//select * from dish where status=1 and category_id=? order by sort asc ,update_time desc
        return R.success(setmealList);
    }
}
