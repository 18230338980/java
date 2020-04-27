package com.steam.datasource.service;

import com.steam.datasource.dto.PlaceOrderRequest;

public interface OrderService {

  /**
   * 下单
   *
   * @param placeOrderRequest 订单请求参数
   */
  void placeOrder(PlaceOrderRequest placeOrderRequest);
}