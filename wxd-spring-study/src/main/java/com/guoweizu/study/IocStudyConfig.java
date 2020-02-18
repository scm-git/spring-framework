package com.guoweizu.study;

import com.test.Bar2Service;
import org.springframework.context.annotation.*;

/**
 * @Configuration注解的类会被解析为一个ConfigurationClass对象
 *
 */

@ComponentScan(basePackages = "com.guoweizu")
@Configuration
@Import(value = Bar2Service.class)
public class IocStudyConfig {


	public Object bean1() {
		return  new Object();
	}
}
