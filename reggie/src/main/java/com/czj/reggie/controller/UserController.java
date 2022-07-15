package com.czj.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.czj.reggie.common.R;
import com.czj.reggie.entity.User;
import com.czj.reggie.service.IUserService;
import com.czj.reggie.utils.SMSUtils;
import com.czj.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 生成和获取手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        //获取手机号
        String phone = user.getPhone();

        //判断是否为空
        if (phone==null){
            return R.error("手机号为空");
        }

        //生成手机验证码(6位数)
        String code = ValidateCodeUtils.generateValidateCode(6).toString();

        //发送验证码短信（因付费注销）
        //SMSUtils.sendMessage("","",phone,code);

        //将验证码存储到session中保存（用于登录时的验证码校验）
        //httpSession.setAttribute("code",code);

        //将手机验证码保存到redis中，有效期5分钟
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

        log.info("手机号：{}，验证码：{}",phone,redisTemplate.opsForValue().get(phone));

        return R.success("手机验证码发送成功");
    }

    /**
     * 登录功能
     * @param user
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody User user){
        log.info("登录phone：{}，验证码：{}",user.getPhone(),user.getCode());

        //获取手机号
        String phone = user.getPhone();

        //校验验证码是否为空
        String lcode = user.getCode();
        if (lcode==null){
            return R.error("验证码为空");
        }

        //从redis中获取手机验证码
        String code = redisTemplate.opsForValue().get(phone).toString();

        //校验验证码是否正确
        if (!code.equals(lcode)){
            return R.error("验证码错误");
        }

        //查询用户,判断是否为新用户
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(User::getPhone,user.getPhone()); //where phone=?
        User ur = userService.getOne(q);
        if (ur==null){
            ur=new User();
            ur.setStatus(1);
            ur.setPhone(user.getPhone());
            //是新用户则主动注册（数据存入数据库）
            userService.save(ur);
            log.info("注册成功");
        }

        //登录成功,将用户id存入session,删除redis中的验证码
        httpSession.setAttribute("user",ur.getId());
        redisTemplate.delete(phone);
        log.info("id:{}",httpSession.getAttribute("user"));
        return R.success(ur);
    }

    @PostMapping("/loginout")
    public R<String> loginout(){
        log.info("用户退出登录id：{}",httpSession.getAttribute("user"));
        httpSession.removeAttribute("user");
        return R.success("退出登录");
    }

}
