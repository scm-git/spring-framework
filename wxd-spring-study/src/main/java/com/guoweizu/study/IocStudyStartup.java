package com.guoweizu.study;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

public class IocStudyStartup {

	@Bean
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
	}
}
