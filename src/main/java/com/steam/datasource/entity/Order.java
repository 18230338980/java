package com.steam.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.steam.datasource.common.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@TableName("p_order")
public class Order {

  @TableId(type = IdType.AUTO)
  private Integer id;

  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 商品ID
   */
  private Long productId;
  /**
   * 订单状态
   */
  private OrderStatus status;
  /**
   * 数量
   */
  private Integer amount;
  /**
   * 总金额
   */
  private Double totalPrice;
}