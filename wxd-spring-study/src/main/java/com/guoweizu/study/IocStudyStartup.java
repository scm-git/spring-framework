package com.guoweizu.study;


import com.guoweizu.study.aop.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocStudyStartup {



	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(IocStudyConfig.class);

		String[] beanDefinitionNames = annotationConfigApplicationContext.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			System.out.println("bean:" + beanDefinitionName);
			if ("bean2".equals(beanDefinitionName)) {
				System.out.println("bean2:" + annotationConfigApplicationContext.getBean(beanDefinitionName).getClass());
			}
		}

		UserService userService = annotationConfigApplicationContext.getBean("userService", UserService.class);
		userService.printName("用户1");
		userService.printNameAndAddress("用户2", new Object());

		FooService fooService = annotationConfigApplicationContext.getBean("fooService", FooService.class);
		fooService.foo();
		//System.out.println(annotationConfigApplicationContext.getDefaultListableBeanFactory().getAliases("set"));

	}
}