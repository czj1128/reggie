package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.R;
import com.czj.reggie.entity.Employee;
import com.czj.reggie.service.IEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    HttpServletRequest request;

    /**
     * 登录功能
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee){
        //将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lqw);
        //判断用户名是否存在
        if (emp==null){
            return R.error("用户名不存在");
        }
        ///判断密码是否正确
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //判断员工状态，是否被禁用
        if (emp.getStatus().toString().equals("0")){
            return R.error("账号已禁用");
        }
        //登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 账号退出功能
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(){
        //清除session中保存的员工id
        Long empId = (Long) request.getSession().getAttribute("employee");
        log.info("用户退出登录，用户id:{}",empId);
        request.getSession().removeAttribute("employee");
        return R.success("退出登录");
    }

    /**
     * 员工信息添加
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee){
        log.info("添加员工，员工信息：{}",employee.toString());
        //添加账号创建时间
        //employee.setCreateTime(LocalDateTime.now());
        //更新账号修改时间
        //employee.setUpdateTime(LocalDateTime.now());
        //创建该账号的用户
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //更新该账号的用户
        //employee.setUpdateUser(empId);

        //设置创建员工账号时的初始密码为123456，通过md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //添加进数据库
        employeeService.save(employee);
        //返回添加成功结果
        return R.success("员工添加成功");
    }

    /**
     * 员工信息分页查询（包含姓名模糊查询）
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> pageList(int page, int pageSize, String name){
        log.info("page：{}，pageSize：{}，name：{}",page,pageSize,name);
        //构造分页构造器
        Page<Employee> empPage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //进行条件过滤
        queryWrapper.like(name!=null,Employee::getName,name);
        //进行条件排序 ：按照员工信息修改时间'降序'排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //进行员工信息查询
        employeeService.page(empPage, queryWrapper);

        return R.success(empPage);
    }

    /**
     * 员工信息修改
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        log.info("员工信息更新内容为：{}"+employee.toString());
        //更新账号修改时间
        //employee.setUpdateTime(LocalDateTime.now());
        //更新该账号的用户
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId);

        //数据修改
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询，id：{}"+id);
        Employee employee = employeeService.getById(id);
        if (employee==null)
            return R.error("员工信息查询失败");
        return R.success(employee);
    }

}
