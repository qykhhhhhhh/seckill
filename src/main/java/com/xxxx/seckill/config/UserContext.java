package com.xxxx.seckill.config;


import com.xxxx.seckill.pojo.User;

public class UserContext {
    private static ThreadLocal<User> userThreadLocal;

    public static void setUser(User user){
        userThreadLocal.set(user);
    }

    public static User getUser(){
        return userThreadLocal.get();
    }
}
