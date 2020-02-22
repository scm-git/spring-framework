package com.guoweizu.study.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceAspect {

	/**
	 * execution: For matching method execution join points. This is the primary pointcut designator to use when working with Spring AOP.
	 * within: Limits matching to join points within certain types (the execution of a method declared within a matching type when using Spring AOP).
	 * this: Limits matching to join points (the execution of methods when using Spring AOP) where the bean reference (Spring AOP proxy) is an instance of the given type.
	 * target: Limits matching to join points (the execution of methods when using Spring AOP) where the target object (application object being proxied) is an instance of the given type.
	 * args: Limits matching to join points (the execution of methods when using Spring AOP) where the arguments are instances of the given types.
	 * @target: Limits matching to join points (the execution of methods when using Spring AOP) where the class of the executing object has an annotation of the given type.
	 * @args: Limits matching to join points (the execution of methods when using Spring AOP) where the runtime type of the actual arguments passed have annotations of the given types.
	 * @within: Limits matching to join points within types that have the given annotation (the execution of methods declared in types with the given annotation when using Spring AOP).
	 * @annotation: Limits matching to join points where the subject of the join point (the method being executed in Spring AOP) has the given annotation.
	 *
	 * 示例：
	 * execution(public * *(..)) // 所有的public方法
	 * execution(* set*(..))	// set开头的方法
	 * execution(* com.xyz.service.AccountService.*(..)) // 所有AccountService中的方法
	 * execution(* com.xyz.service.*.*(..))	// 所有com.xyz.service包中的方法(不包括子包)
	 * execution(* com.xyz.service..*.*(..)) // 所有com.xyz.service包及其子包中的方法
	 * within(com.xyz.service.*)	// 所有com.xyz.service包中的方法(不包括子包)
	 * within(com.xyz.service..*)	// 所有com.xyz.service包及其子包中的方法
	 * this(com.xyz.service.AccountService)	// 所有实现了AccountService的proxy
	 * target(com.xyz.service.AccountService)	// 所有实现了AccountService的proxy, 同this
	 * args(java.io.Serializable)	// 参数类型为Serializable的方法
	 * @target(org.springframework.transaction.annotation.Transactional) 	// 有@Transactional注解的方法
	 * @within(org.springframework.transaction.annotation.Transactional)	// 有@Transactional注解的方法
	 * @annotation(org.springframework.transaction.annotation.Transactional)	// 有@Transactional注解的方法
	 * @args(com.xyz.security.Classified)	// 参数带有@Classified注解的方法
	 * bean(fooService) 名为fooService的spring bean
	 * bean(*Service)  所有以Service结尾的spring bean
	 *
	 */
	public void pointcut() {

	}

	/**
	 * 定义切入点为所有的public方法
	 */
	@Pointcut("execution( public * *(..))")
	private void anyPublicOperation() {}

	/**
	 * 定义切入点为com.guoweizu.study.aop包下的所有方法
	 */
	@Pointcut("within(com.guoweizu.study.aop.service..*)")
	private void inStudyAop() {}

	/**
	 * 定义切入点为anyPublicOperation和inStudyAop的组合
	 */
	@Pointcut(value = "anyPublicOperation() && inStudyAop()")
	private void inStudyAopPublicOperation() {}


	/**
	 * 在调用方法之前执行
	 */
	// 引用pointcut时，方法名后面必须添加括号
	//@Before(value = "execution(public * com.guoweizu..*.*(..))")
	//@Before("execution(* com.guoweizu..*.*(..))")
	//@Before("execution(* com.guoweizu..*(..))")
	@Before(value = "inStudyAopPublicOperation()")
	public void before() {
		System.out.println("before 通知。。。。");
	}

	/**
	 * 在finally中执行，不管是否抛异常，都会执行
	 */
	@After(value = "inStudyAopPublicOperation()")
	public void after() {
		System.out.println("after 通知。。。。");
	}

	/**
	 * 在return之前执行，抛异常就不会执行
	 * 不抛异常才会执行，且不抛在@After之前执行，因为finally在return语句之前执行
	 */
	@AfterReturning(value = "inStudyAopPublicOperation()")
	public void afterReturning() {
		System.out.println("afterReturning 通知。。。");
	}

	/**
	 * 当方法抛出异常时执行,
	 * 如果方法中catch了异常，且catch语句块中没有再抛出，那就不会执行，因为catch就相当于方法正常执行了
	 */
	@AfterThrowing(value = "inStudyAopPublicOperation()")
	public void afterThrowing() {
		System.out.println("after throwing 通知。。。");
	}

	/**
	 * 环绕通知，能力最强的一种通知，因为在执行方法的前后可以任意添加切面代码
	 * 需要注意以下几点：
	 * 1. 在joinPoint.proceed()之前的代码，会在@Before之前
	 * 2. 在joinPoint.proceed()之后的代码，在@After, @AfterReturning之后执行
	 * 3. 如果proceed()抛出异常，则不会执行proceed()之后的代码，且异常通知@AfterThrowing会执行
	 *
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around(value = "inStudyAopPublicOperation()")
	public void around(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("around before calling join point。。。");
		Object[] args = joinPoint.getArgs();
		System.out.println("param:" + Arrays.asList(args));
		Object proceed = joinPoint.proceed();
		System.out.println("around after calling join point。。。" + proceed);
	}

	@Before(value = "@annotation(com.guoweizu.study.aop.annotation.Loggable)")
	public void log(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		System.out.println("input param:" + Arrays.asList(args));
	}


}
