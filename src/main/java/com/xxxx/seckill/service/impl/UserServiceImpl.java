package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author hardy
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     *
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();


//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//
//        //如果不是手机号
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }

        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        //如果查不到用户
        if (null == user) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //如果用户不为空，校验密码
        //传入的数据进行二次加密 与数据库的密码进行比较
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            //返回用户名或者密码错误
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生成cookie
        String uuid = UUIDUtil.uuid();

        //HttpSession session = request.getSession();
        //session.setAttribute(uuid, user);

        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+uuid,user);
        CookieUtil.setCookie(request, response, "uuid", uuid);
        return RespBean.success(uuid);
    }

    @Override
    public User getUserByCookie(String uuid,HttpServletRequest request, HttpServletResponse response) {
        User user = (User) redisTemplate.opsForValue().get("user:" + uuid);
        if (user!=null){
            CookieUtil.setCookie(request,response,"uuid",uuid);
        }
        return user;
    }


    @Override
    public RespBean updatePassword(String uuid, String password,HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(uuid, request, response);
        if (user==null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.formPassToDBPass(password,user.getSalt()));
        int result = userMapper.updateById(user);
        if (1==result){
            redisTemplate.delete("user:"+uuid);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWROD_UPDATE_FAIL);
    }
}

