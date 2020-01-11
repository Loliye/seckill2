package com.mikufans.seckill.common.util;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpClient
{
    public String client(String url, HttpMethod method, MultiValueMap<String,String> params)
    {
        RestTemplate client=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();

        //提交方式大多都是表单
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String,String>> requestEntity=new HttpEntity<>(params,headers);
        //执行http请求
        ResponseEntity<String> response=client.exchange(url, HttpMethod.POST,requestEntity,String.class);
        return response.getBody();
    }
}
