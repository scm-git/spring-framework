package com.guoweizu.study.aop.service;

public interface IOrderService {
	void purchase(String productId);

	void refund(String orderId);
}
