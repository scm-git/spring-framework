package com.guoweizu.study.aop.service;

import com.guoweizu.study.aop.annotation.LogInput;
import com.guoweizu.study.aop.annotation.LogInputAndOutput;
import com.guoweizu.study.aop.annotation.LogOutput;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService{
	@LogInput
	public void printName(String name) {
		System.out.println("user.name:"  + name);
		try {
			throw new RuntimeException("exception...");
		} catch (RuntimeException e) {
			// nothing
			//throw e;
		}
	}

	@LogInput
	public void printNameAndAddress(String name, Object address ) {
		System.out.println("printNameAndAddress:" + name + ", address:" + address);
	}

	@LogInput
	@LogOutput
	public String getName(String name) {
		return "姓名:" + name;
	}

	@LogInputAndOutput
	public String walk(String from, String to) {
		return "from " + from + " to " + to;
	}

	@LogInputAndOutput
	public void goHome() {
		//System.out.println("go home before");
		//((UserService)AopContext.currentProxy()).printName("name in home");
		//System.out.println("go home after");
	}


}

