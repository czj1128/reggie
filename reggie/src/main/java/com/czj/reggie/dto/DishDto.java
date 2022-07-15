package com.czj.reggie.dto;

import com.czj.reggie.entity.Dish;
import com.czj.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors ;

    private String categoryName;

    private Integer copies;
}
