package com.guoweizu.study.aop.service;

import com.guoweizu.study.aop.annotation.Loggable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	@Loggable
	public void printName(String name) {
		System.out.println("user.name:"  + name);
		try {
			throw new RuntimeException("exception...");
		} catch (RuntimeException e) {
			// nothing
			//throw e;
		}
	}

	@Loggable
	public void printNameAndAddress(String name, Object address ) {
		System.out.println("printNameAndAddress:" + name + ", address:" + address);
	}


}

