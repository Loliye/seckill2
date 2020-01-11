package com.mikufans.seckill.web;

import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.service.CreateHtmlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "生成静态商品页面")
@RestController
@RequestMapping("/createHtml")
public class CreateHtmlController
{
    @Autowired
    private CreateHtmlService createHtmlService;

    @ApiOperation(value = "生成静态页面")
    @PostMapping("/start")
    public Result start()
    {
        return createHtmlService.creatAllHtml();
    }
}
