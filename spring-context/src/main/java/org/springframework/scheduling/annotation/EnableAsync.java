/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Enables Spring's asynchronous method execution capability, similar to functionality
 * found in Spring's {@code <task:*>} XML namespace.
 *
 * <p>To be used together with @{@link Configuration Configuration} classes as follows,
 * enabling annotation-driven async processing for an entire Spring application context:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig {
 *
 * }</pre>
 *
 * {@code MyAsyncBean} is a user-defined type with one or more methods annotated with
 * either Spring's {@code @Async} annotation, the EJB 3.1 {@code @javax.ejb.Asynchronous}
 * annotation, or any custom annotation specified via the {@link #annotation} attribute.
 * The aspect is added transparently for any registered bean, for instance via this
 * configuration:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AnotherAppConfig {
 *
 *     &#064;Bean
 *     public MyAsyncBean asyncBean() {
 *         return new MyAsyncBean();
 *     }
 * }</pre>
 *
 * <p>By default, Spring will be searching for an associated thread pool definition:
 * either a unique {@link org.springframework.core.task.TaskExecutor} bean in the context,
 * or an {@link java.util.concurrent.Executor} bean named "taskExecutor" otherwise. If
 * neither of the two is resolvable, a {@link org.springframework.core.task.SimpleAsyncTaskExecutor}
 * will be used to process async method invocations. Besides, annotated methods having a
 * {@code void} return type cannot transmit any exception back to the caller. By default,
 * such uncaught exceptions are only logged.
 *
 * <p>To customize all this, implement {@link AsyncConfigurer} and provide:
 * <ul>
 * <li>your own {@link java.util.concurrent.Executor Executor} through the
 * {@link AsyncConfigurer#getAsyncExecutor getAsyncExecutor()} method, and</li>
 * <li>your own {@link org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
 * AsyncUncaughtExceptionHandler} through the {@link AsyncConfigurer#getAsyncUncaughtExceptionHandler
 * getAsyncUncaughtExceptionHandler()}
 * method.</li>
 * </ul>
 *
 * <p><b>NOTE: {@link AsyncConfigurer} configuration classes get initialized early
 * in the application context bootstrap. If you need any dependencies on other beans
 * there, make sure to declare them 'lazy' as far as possible in order to let them
 * go through other post-processors as well.</b>
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig implements AsyncConfigurer {
 *
 *     &#064;Override
 *     public Executor getAsyncExecutor() {
 *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *         executor.setCorePoolSize(7);
 *         executor.setMaxPoolSize(42);
 *         executor.setQueueCapacity(11);
 *         executor.setThreadNamePrefix("MyExecutor-");
 *         executor.initialize();
 *         return executor;
 *     }
 *
 *     &#064;Override
 *     public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
 *         return new MyAsyncUncaughtExceptionHandler();
 *     }
 * }</pre>
 *
 * <p>If only one item needs to be customized, {@code null} can be returned to
 * keep the default settings. Consider also extending from {@link AsyncConfigurerSupport}
 * when possible.
 *
 * <p>Note: In the above example the {@code ThreadPoolTaskExecutor} is not a fully managed
 * Spring bean. Add the {@code @Bean} annotation to the {@code getAsyncExecutor()} method
 * if you want a fully managed bean. In such circumstances it is no longer necessary to
 * manually call the {@code executor.initialize()} method as this will be invoked
 * automatically when the bean is initialized.
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;task:annotation-driven executor="myExecutor" exception-handler="exceptionHandler"/&gt;
 *
 *     &lt;task:executor id="myExecutor" pool-size="7-42" queue-capacity="11"/&gt;
 *
 *     &lt;bean id="asyncBean" class="com.foo.MyAsyncBean"/&gt;
 *
 *     &lt;bean id="exceptionHandler" class="com.foo.MyAsyncUncaughtExceptionHandler"/&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * The above XML-based and JavaConfig-based examples are equivalent except for the
 * setting of the <em>thread name prefix</em> of the {@code Executor}; this is because
 * the {@code <task:executor>} element does not expose such an attribute. This
 * demonstrates how the JavaConfig-based approach allows for maximum configurability
 * through direct access to actual componentry.
 *
 * <p>The {@link #mode} attribute controls how advice is applied: If the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior
 * of the proxying. Please note that proxy mode allows for interception of calls through
 * the proxy only; local calls within the same class cannot get intercepted that way.
 *
 * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the
 * value of the {@link #proxyTargetClass} attribute will be ignored. Note also that in
 * this case the {@code spring-aspects} module JAR must be present on the classpath, with
 * compile-time weaving or load-time weaving applying the aspect to the affected classes.
 * There is no proxy involved in such a scenario; local calls will be intercepted as well.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see Async
 * @see AsyncConfigurer
 * @see AsyncConfigurationSelector
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncConfigurationSelector.class)
public @interface EnableAsync {
	/**
	 * 看类上的注释：
	 * 1. 默认查找beanName为"taskExecutor"的{@link org.springframework.core.task.TaskExecutor}的bean，如果找不到
	 *    则查找beanName为"taskExecutor"的{@link java.util.concurrent.Executor}的bean，如果也查找不到
	 *    则spring默认使用{@link org.springframework.core.task.SimpleAsyncTaskExecutor}
	 * 2. 被注解的方法如果是返回void，那么executor不会传递异常信息给调用者，仅仅是在日志中打印异常信息
	 * 3. 如果想要自己定制以上信息：比如executor和异常；可以自己实现{@link AsyncConfigurer}，或者继承其子类{@link AsyncConfigurerSupport}
	 *    不需要定制的直接返回null
	 * 4. 如果通过第3步的方式定制，且想要将executor放入spring bean中，想要加@Bean注解，这样不用自己调用executor.initialize()方法 TODO
	 * 5. 异步执行底层也是代理模式来实现的，所以如果想要其生效，不能再同一个类中调用(因为不会被拦截，跟事务是一个道理)，
	 *
	 * 注册流程：
	 * 1. refresh第5不扫描所有@Import注解，并注册这些BeanDefinition, 此处的AsyncConfigurationSelector返回{@link ProxyAsyncConfiguration}，所以会注册这个组件
	 * 2. ProxyAsyncConfiguration中有一个@Bean注解方法，所以又会扫描到该方法，并注册这个@Bean方法定义的组件：{@link AsyncAnnotationBeanPostProcessor}
	 * 3. 查看第二步中的{@link AsyncAnnotationBeanPostProcessor}可以发现他是一个BeanFactoryAware的实现类 -- 所以在实例化bean时，在initializeBean方法中会调用其setBeanFactory方法
	 *    这个实例化步骤在refresh第6步，会注册refresh第5步中扫描到的所有BeanPostProcessor, 此处就会调用getBean()方法实例化这些BeanPostProcessor， 所以这个创建在其他非BeanPostProcessor的bean之前创建
	 *    其他的bean在refresh第11步创建
	 * 4. 查看{@link AsyncAnnotationBeanPostProcessor#setBeanFactory(BeanFactory)}方法，可以看到，里面创建了AsyncAnnotationAdvisor -- 是一个Advisor（该步骤在创建bean的initializeBean中的invokeAware方法中执行）
	 * 5. 第4步执行完成后，initializeBean中会继续调用{@link AsyncAnnotationBeanPostProcessor#postProcessAfterInitialization(Object, String)}方法，该方法会为适合的bean创建代理对象
	 * 6. 所以refresh第11步初始化POJO bean的时候就能获取到这个前面注册的Advisor了
	 * 7. 自己定制{@link AsyncConfigurer},  ProxyAsyncConfiguration这个类的父类AbstractAsyncConfiguration有一个@Autowired的setConfigurer方法，所以在实例化这个bean的时候
	 *    在populateBean步骤就会调用此方法，该方法的入参是AspectConfigurer，所以会找到所有其子类的bean，并调用此方法，这样就完成了自己定制的executor和exceptionHandler设置
	 * 8. 第4步中创建的AsyncAnnotationAdvisor中的advice属性是一个{@link AnnotationAsyncExecutionInterceptor}其父类中的invoke方法中，会将method调用包装成一个Callable并提交给executor执行
	 *    这个Callable会根据方法的返回值构建如下4种Callable:
	 *    * CompletableFuture
	 *    * ListenableFuture
	 *    * Future
	 *    * 普通Runnable
	 *
	 */

	/**
	 * Indicate the 'async' annotation type to be detected at either class
	 * or method level.
	 * <p>By default, both Spring's @{@link Async} annotation and the EJB 3.1
	 * {@code @javax.ejb.Asynchronous} annotation will be detected.
	 * <p>This attribute exists so that developers can provide their own
	 * custom annotation type to indicate that a method (or all methods of
	 * a given class) should be invoked asynchronously.
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies.
	 * <p><strong>Applicable only if the {@link #mode} is set to {@link AdviceMode#PROXY}</strong>.
	 * <p>The default is {@code false}.
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with {@code @Async}.
	 * For example, other beans marked with Spring's {@code @Transactional} annotation
	 * will be upgraded to subclass proxying at the same time. This approach has no
	 * negative impact in practice unless one is explicitly expecting one type of proxy
	 * vs. another &mdash; for example, in tests.
	 */
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how async advice should be applied.
	 * <p><b>The default is {@link AdviceMode#PROXY}.</b>
	 * Please note that proxy mode allows for interception of calls through the proxy
	 * only. Local calls within the same class cannot get intercepted that way; an
	 * {@link Async} annotation on such a method within a local call will be ignored
	 * since Spring's interceptor does not even kick in for such a runtime scenario.
	 * For a more advanced mode of interception, consider switching this to
	 * {@link AdviceMode#ASPECTJ}.
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the order in which the {@link AsyncAnnotationBeanPostProcessor}
	 * should be applied.
	 * <p>The default is {@link Ordered#LOWEST_PRECEDENCE} in order to run
	 * after all other post-processors, so that it can add an advisor to
	 * existing proxies rather than double-proxy.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

}
