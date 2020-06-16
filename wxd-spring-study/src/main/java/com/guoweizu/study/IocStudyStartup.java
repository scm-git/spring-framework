package com.guoweizu.study;


import com.guoweizu.study.aop.service.IOrderService;
import com.guoweizu.study.aop.service.IUserService;
import com.guoweizu.study.aop.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IocStudyStartup {



	public static void main(String[] args) {
		// AnnotationConfigApplicationContext annotationConfigApplicationContext1 = new AnnotationConfigApplicationContext("com.guoweizu", "com.other");
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(IocStudyConfig.class);

		String[] beanDefinitionNames = annotationConfigApplicationContext.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			System.out.println("bean:" + beanDefinitionName);
			if ("bean2".equals(beanDefinitionName)) {
				System.out.println("bean2:" + annotationConfigApplicationContext.getBean(beanDefinitionName).getClass());
			}
		}

		System.out.println("=============AOP===============\n");
		IUserService userService = (IUserService) annotationConfigApplicationContext.getBean("userService");
		userService.printName("用户1");
		System.out.println("=============1结束===============\n");
		userService.printNameAndAddress("用户2", "重庆");
		System.out.println("=============2结束===============\n");
		userService.getName("用户3");
		System.out.println("=============3结束===============\n");
		userService.walk("重庆", "成都");
		System.out.println("=============4结束===============\n");
		userService.goHome();
		System.out.println("=============4结束===============\n");

		IOrderService orderService = annotationConfigApplicationContext.getBean("orderService", IOrderService.class);
		orderService.purchase("苹果");

		FooService fooService = annotationConfigApplicationContext.getBean("fooService", FooService.class);
		fooService.foo();
		//System.out.println(annotationConfigApplicationContext.getDefaultListableBeanFactory().getAliases("set"));

	}
}