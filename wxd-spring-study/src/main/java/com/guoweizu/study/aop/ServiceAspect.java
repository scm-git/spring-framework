package com.guoweizu.study.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
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

	@Pointcut(value = "@annotation(com.guoweizu.study.aop.annotation.LogInput)")
	private void logInput(){}

	@Pointcut(value = "@annotation(com.guoweizu.study.aop.annotation.LogInputAndOutput)")
	private void logInputAndOutput() {}


	/**
	 * 在调用方法之前执行
	 */
	// 引用pointcut时，方法名后面必须添加括号
	//@Before(value = "execution(public * com.guoweizu..*.*(..))")
	//@Before("execution(* com.guoweizu..*.*(..))")
	//@Before("execution(* com.guoweizu..*(..))")
	@Before(value = "inStudyAopPublicOperation()")
	public void before() {
		System.out.println("[@Before],");
	}

	/**
	 * 在finally中执行，不管是否抛异常，都会执行
	 */
	@After(value = "inStudyAopPublicOperation()")
	public void after() {
		System.out.println("[@After],");
	}

	/**
	 * 在return之前执行，抛异常就不会执行
	 * 不抛异常才会执行，且不抛在@After之前执行，因为finally在return语句之前执行
	 */
	@AfterReturning(value = "inStudyAopPublicOperation()")
	public void afterReturning() {
		System.out.println("[@AfterReturning],");
	}

	/**
	 * 当方法抛出异常时执行,
	 * 如果方法中catch了异常，且catch语句块中没有再抛出，那就不会执行，因为catch就相当于方法正常执行了
	 */
	@AfterThrowing(value = "inStudyAopPublicOperation()")
	public void afterThrowing() {
		System.out.println("[@AfterThrowing],");
	}

	/**
	 * 环绕通知，能力最强的一种通知，因为在执行方法的前后可以任意添加切面代码
	 * 需要注意以下几点：
	 * 1. around通知一定要返回proceed()方法的执行结果，否则方法被拦截后，返回值没有返回，调用者就拿不到原方法return的结果了
	 * 2. 多个around通知会包裹执行
	 * 3. 执行顺序：
	 * 		1. @Around里面pjp.proceed()之前的代码
	 * 		2. @Before
	 * 		3. @Around里面pjp.proceed() （实际的业务代码）
	 * 		4. @AfterThrowing (5,7不会执行)
	 * 		5. @Around里面pjp.proceed()之后的代码
	 * 		6. @After (finally)
	 * 		7. @AfterReturning (4不会执行)
	 * @param pjp
	 * @throws Throwable
	 */
	@Around(value = "inStudyAopPublicOperation()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();
		System.out.println("[@Around - proceed之前], input: " + Arrays.asList(args));
		Object proceed = pjp.proceed();
		System.out.println("[@Around - proceed之后], output: " + proceed);
		return proceed;
	}

	//@Before(value = "@annotation(com.guoweizu.study.aop.annotation.Loggable)")
	@Before(value = "logInput()")
	public void logInput(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		System.out.println("[@Before], log input: " + Arrays.asList(args));
	}

	@AfterReturning(returning = "output", value = "@annotation(com.guoweizu.study.aop.annotation.LogOutput)")
	public void logOutput(Object output) {
		System.out.println("[@AfterReturning], log output: " + output);
	}

	@Around(value = "logInputAndOutput()")
	public Object logInputAndOutput(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
		Object[] args = proceedingJoinPoint.getArgs();
		Signature signature = proceedingJoinPoint.getSignature();
		System.out.println("["+signature.toShortString()+"], input: " + Arrays.asList(args));
		Object proceed = proceedingJoinPoint.proceed();
		System.out.println("["+signature.toShortString()+"], output: " + proceed);
		return proceed;
	}

}
