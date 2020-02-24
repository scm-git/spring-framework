package com.guoweizu.study.aop.service;

import com.guoweizu.study.aop.annotation.LogInput;
import com.guoweizu.study.aop.annotation.LogInputAndOutput;
import com.guoweizu.study.aop.annotation.LogOutput;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
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
		return "from " + from + " " + to;
	}

	@LogInputAndOutput
	public void goHome() {
	}


}

