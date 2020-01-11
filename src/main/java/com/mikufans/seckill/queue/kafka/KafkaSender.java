package com.mikufans.seckill.queue.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * kafka生产者
 */
@Component
public class KafkaSender
{
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendChannelMess(String channel, String message)
    {
        kafkaTemplate.send(channel, message);
    }
}
