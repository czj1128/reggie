package com.czj.reggie.config;

import com.czj.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开启静态资源映射");
        //后台静态页面资源
        registry.addResourceHandler("backend/**").addResourceLocations("classpath:/backend/");
        //移动端静态页面资源
        registry.addResourceHandler("front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩建mvc框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器   Jackson将java转换成json格式
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将创建的消息转换器添加到mvc框架转换器的集合中去
        converters.add(0,messageConverter);
    }
}
