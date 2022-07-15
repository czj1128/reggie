package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.R;
import com.czj.reggie.dto.DishDto;
import com.czj.reggie.entity.Category;
import com.czj.reggie.entity.Dish;
import com.czj.reggie.entity.DishFlavor;
import com.czj.reggie.service.ICategoryService;
import com.czj.reggie.service.IDishFlavorService;
import com.czj.reggie.service.IDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private IDishService dishService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IDishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 菜品添加
     * @param dishDto
     * @return
     */
    @PostMapping
    private R<String> save(@RequestBody DishDto dishDto){
        //清理数据缓存
        String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);

        log.info("添加dishDto：{}",dishDto);
        dishService.saveCZJ(dishDto);
        return R.success("菜品添加成功");
    }

    /**
     * 分页查询，菜品展示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){
        //分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据菜品名称模糊查询 where like name= %？%
        queryWrapper.like(name!=null,Dish::getName,name);
        //根据菜品修改日期’降序‘排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(dishPage,queryWrapper);

        //对象拷贝  可排除需要拷贝的属性值 如：dishPage对象中的records属性值
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        //获取dishPage对象中的records属性值
        List<Dish> records = dishPage.getRecords();
        //为records中添加菜品分类名称
        List<DishDto> dtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //item的值拷贝给dishDto对象
            BeanUtils.copyProperties(item, dishDto);
            //通过菜品表dish中的菜品分类id：idcategory_id获取分类信息表category中菜品分类类型的对应菜品表中的菜品分类名称
            Long categoryId = dishDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        //将添加好菜品分类名称的dtoLists赋值给dishDtoPage对象
        dishDtoPage.setRecords(dtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id获取菜品信息和菜品口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        log.info("菜品信息，id：{}",id);
        DishDto dishDto = dishService.getByIdCZJ(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息和菜品口味信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        //清理数据缓存
        String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);

        log.info("修改菜品信息：{}",dishDto);
        dishService.updateCZJ(dishDto);
        return R.success("菜品修改成功");
    }

    /**
     * 批量删除菜品信息和菜品口味信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deletes(String ids){
        //清理数据缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        log.info("删除菜品，id：{}",ids);
        String[] sids = ids.split(",");
        Long[] lids=new Long[sids.length];
        for (int i=0;i<sids.length;i++){
            lids[i]=Long.valueOf(sids[i]);
        }
        dishService.removeByIdsCZJ(lids);
        return R.success("菜品删除成功");
    }

    /**
     * 批量修改菜品售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status,@RequestParam List<Long> ids){
        //清理数据缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        log.info("菜品售卖状态修改，status：{}，ids：{}",status,ids);
        /*String[] sids = ids.split(",");
        ArrayList<Dish> dishs = new ArrayList<>();
        for (int i=0;i<sids.length;i++){
            Dish dish = new Dish();
            //获得菜品id
            dish.setId(Long.valueOf(sids[i]));
            //修改菜品状态
            dish.setStatus(status);
            dishs.add(dish);
        }*/
        //dishService.updateBatchById(dishs);
        dishService.updateStatus(status,ids);
        return R.success("售卖状态修改成功");
    }

    /**
     * 根据分类id查询对应菜品类型中的所有菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        log.info("菜品类型id：{}",dish.getCategoryId());

        List<DishDto> dtoList=null;

        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        //从redis缓存中查询菜品
        dtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果redis有菜品缓存，无需查询数据库
        if (dtoList!=null){
            return R.success(dtoList);
        }

        //如果redis没有菜品缓存，查询数据库

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询当前菜品分类中所有菜品
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());// where category_id=?
        //只查询起售状态（status=1）的菜品
        queryWrapper.eq(Dish::getStatus,1); // where status=1
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);//order by sort asc ,update_time desc
        //执行查询
        List<Dish> dishList = dishService.list(queryWrapper);//select * from dish where status=1 and category_id=? order by sort asc ,update_time desc

        dtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //item的值拷贝给dishDto对象
            BeanUtils.copyProperties(item, dishDto);
            //通过菜品表dish中的菜品分类id：idcategory_id获取分类信息表category中菜品分类类型的对应菜品表中的菜品分类名称
            Long categoryId = dishDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            //获取菜品id
            Long id = item.getId();
            //查询菜品对应的口味
            List<DishFlavor> dishFlavorList = dishFlavorService.list(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, id));//where dish_id=?
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //并将数据放入redis缓存
        redisTemplate.opsForValue().set(key,dishList,60, TimeUnit.MINUTES);

        return R.success(dtoList);
    }

    /**
     * 根据分类id查询对应菜品类型中的所有菜品
     * @param dish
     * @return
     *//*
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        log.info("菜品类型id：{}",dish.getCategoryId());

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询当前菜品分类中所有菜品
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());// where category_id=?
        //只查询起售状态（status=1）的菜品
        queryWrapper.eq(Dish::getStatus,1); // where status=1
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);//order by sort asc ,update_time desc
        //执行查询
        List<Dish> dishList = dishService.list(queryWrapper);//select * from dish where status=1 and category_id=? order by sort asc ,update_time desc
        return R.success(dishList);
    }*/
}
