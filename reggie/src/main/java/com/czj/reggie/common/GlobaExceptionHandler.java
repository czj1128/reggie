package com.czj.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class}) //拦截添加了指定注解的类
@ResponseBody
@Slf4j
public class GlobaExceptionHandler {
    /**
     * 异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> ExceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        if (e.getMessage().contains("Duplicate entry")){
            String[] s = e.getMessage().split(" ");
            return R.error(s[2]+"已存在");
        }
        return R.error("未知错误");
    }

    /**
     * 自定义异常的异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> ExceptionHandler(CustomException e){
        log.error(e.getMessage());
        return R.error(e.getMessage());
    }


}
