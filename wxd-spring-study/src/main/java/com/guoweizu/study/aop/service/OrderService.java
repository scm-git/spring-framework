package com.guoweizu.study.aop.service;

import com.guoweizu.study.aop.annotation.LogInputAndOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService implements IOrderService {
	@Autowired
	IUserService userService;

	@Override
	@LogInputAndOutput
	public void purchase(String productId) {
		System.out.println("开始购买商品:" + productId);
		userService.walk("重庆", "成都");
		System.out.println("购买完成:");
	}

	@Override
	@LogInputAndOutput
	public void refund(String orderId) {
		System.out.println("开始退货:" + orderId);
		userService.goHome();
		System.out.println("退货完成:" + orderId);
	}

}
