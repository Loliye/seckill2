package com.mikufans.seckill.repository;

import com.mikufans.seckill.common.entity.Seckill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillRepository  extends JpaRepository<Seckill,Long>
{
}
