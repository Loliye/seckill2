package com.mikufans.seckill.web;

import com.mikufans.seckill.common.entity.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Api(tags = "抢红包")
@RestController
@RequestMapping("/redPacket")
public class RedPacketController
{
    @ApiOperation(value="抢红包一(最low实现)")
    @PostMapping("/start")
    public static Result start(long seckillId)
    {
        return Result.ok();
    }
}
