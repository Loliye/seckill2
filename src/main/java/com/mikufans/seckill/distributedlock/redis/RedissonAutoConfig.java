package com.mikufans.seckill.distributedlock.redis;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(RedissonProperites.class)
public class RedissonAutoConfig
{
    @Autowired
    private RedissonProperites redissonProperites;


    /**
     * 单机模式自动装配
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    RedissonClient redissonSingle()
    {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redissonProperites.getAddress())
                .setTimeout(redissonProperites.getTimeout())
                .setConnectionPoolSize(redissonProperites.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperites.getConnectionMinimumIdleSize());
        if (StringUtils.isNotBlank(redissonProperites.getPassword()))
            serverConfig.setPassword(redissonProperites.getPassword());

        return Redisson.create(config);
    }

    /**
     * 哨兵模式自动装配
     * @return
     */
//    @Bean
//    @ConditionalOnProperty(name = "redisson.master-name")
    RedissonClient redissonSentinel()
    {
        Config config=new Config();
        SentinelServersConfig serversConfig=config.useSentinelServers().addSentinelAddress(redissonProperites.getSentinelAddresses())
                .setMasterName(redissonProperites.getMasterName())
                .setTimeout(redissonProperites.getTimeout())
                .setMasterConnectionPoolSize(redissonProperites.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redissonProperites.getSlaveConnectionPoolSize());

        if(StringUtils.isNotBlank(redissonProperites.getPassword()))
            serversConfig.setPassword(redissonProperites.getPassword());
        return Redisson.create(config);
    }

    @Bean
    RedissLockUtil redissLockUtil(RedissonClient redissonClient)
    {
        RedissLockUtil redissLockUtil = new RedissLockUtil();
        redissLockUtil.setRedissonClient(redissonClient);
        return redissLockUtil;
    }
}
