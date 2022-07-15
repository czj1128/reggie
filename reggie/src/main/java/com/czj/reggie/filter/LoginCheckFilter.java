package com.czj.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czj.reggie.common.BaseContext;
import com.czj.reggie.common.R;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户登录过滤器
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器
    private static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1.获取本次请求的URL
        String requestURL = request.getRequestURI();
        log.info("拦截到请求：{}",requestURL);

        //2.定义不需要拦截的请求
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //3.判断本次请求是否需要过滤拦截
        boolean check = check(urls, requestURL);
        if (check){
            //如果不需要处理直接放行
            log.info("请求{}不处理",requestURL);
            filterChain.doFilter(request,response);
            return;
        }

        //4.判断登录状态（后台管理）
        if (request.getSession().getAttribute("employee")!=null){
            Long empId = (Long) request.getSession().getAttribute("employee");
            log.info("用户放行，用户id:{}",empId);
            //将用户id存入线程局部变量
            BaseContext.setCurrentId(empId);
            //登录了直接放行
            filterChain.doFilter(request,response);
            return;
        }

        //4.判断登录状态（移动端）
        if (request.getSession().getAttribute("user")!=null){
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("用户放行，用户id:{}",userId);
            //将用户id存入线程局部变量
            BaseContext.setCurrentId(userId);
            //登录了直接放行
            filterChain.doFilter(request,response);
            return;
        }

        //5.未登录返回未登录结果
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    /**
     * 判断请求是否需要拦截
     * @param urls
     * @param requestURL
     * @return
     */
    public boolean check(String[] urls,String requestURL){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
