package com.xxxx.seckill.rabbitmq;


import com.xxxx.seckill.pojo.SeckillMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendSeckillMessage(String message){
        System.out.println("发送消息："+message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }

}
