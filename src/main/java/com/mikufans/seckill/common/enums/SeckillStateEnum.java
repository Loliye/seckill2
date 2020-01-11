package com.mikufans.seckill.common.enums;

/**
 * 秒杀结果 返回给页面
 */
public enum  SeckillStateEnum
{
    MUCH(2,"哎呦喂，人也太多了，请稍后！"),
    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATE_REWRITE(-3,"数据篡改");

    private int state;
    private String info;

    SeckillStateEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }


    public String getInfo() {
        return info;
    }


    public static SeckillStateEnum stateOf(int index)
    {
        for (SeckillStateEnum state : values())
        {
            if (state.getState()==index)
            {
                return state;
            }
        }
        return null;
    }
}
