package com.guoweizu.study;


import com.test.Bar2Service;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class IocStudyStartup {

	public Object bean1() {
		return  new Object();
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(IocStudyStartup.class);
		Object bean1 = annotationConfigApplicationContext.getBean("bean1");
		Object bean2 = annotationConfigApplicationContext.getBean("bean1");

		System.out.println(bean1 == bean2);

		String[] beanDefinitionNames = annotationConfigApplicationContext.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			System.out.println("bean:" + beanDefinitionName);
		}

		annotationConfigApplicationContext.scan("com.guoweizu");
		System.out.println("after scan com.guoweizu package...");
		for (String beanDefinitionName : annotationConfigApplicationContext.getBeanDefinitionNames()) {
			System.out.println("bean:" + beanDefinitionName);
		}

		annotationConfigApplicationContext.registerBean("bar2Service", Bar2Service.class);
		System.out.println("after scan com.guoweizu package...");
		for (String beanDefinitionName : annotationConfigApplicationContext.getBeanDefinitionNames()) {
			System.out.println("bean:" + beanDefinitionName);
		}
	}
}