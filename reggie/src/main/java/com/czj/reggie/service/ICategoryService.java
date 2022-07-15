package com.czj.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czj.reggie.entity.Category;

public interface ICategoryService extends IService<Category> {

    public void removeByIdCZJ(Long id);
}
