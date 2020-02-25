package com.guoweizu.study;

import com.test.Bar2Service;
import org.springframework.context.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Configuration注解的类会被解析为一个ConfigurationClass对象
 *
 */

@ComponentScan(basePackages = "com.guoweizu")
@Configuration
@Import(value = Bar2Service.class)
@EnableAspectJAutoProxy(exposeProxy = true)
public class IocStudyConfig {

	@Bean(name = "bean2")
	public Set<String> set() {
		System.out.println("bean2");
		return new HashSet<>();
	}

	@Bean(name = "bean2")
	public List<String> list() {
		System.out.println("bean3");
		return new ArrayList<>();
	}

	public Object bean1() {
		return  new Object();
	}
}
