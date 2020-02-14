/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.context.annotation;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 实现的接口：
 * 	Closeable,
 * 	AutoCloseable,
 * 	BeanFactory,
 * 	HierarchicalBeanFactory,
 * 	ListableBeanFactory,
 * 	BeanDefinitionRegistry,
 * 	AnnotationConfigRegistry,
 * 	ApplicationContext,
 * 	ApplicationEventPublisher,
 * 	ConfigurableApplicationContext,
 * 	Lifecycle,
 * 	MessageSource,
 * 	AliasRegistry,
 * 	EnvironmentCapable,
 *  ResourceLoader,
 * 	ResourcePatternResolver
 *
 * Standalone application context, accepting <em>component classes</em> as input &mdash;
 * in particular {@link Configuration @Configuration}-annotated classes, but also plain
 * {@link org.springframework.stereotype.Component @Component} types and JSR-330 compliant
 * classes using {@code javax.inject} annotations.
 *
 * <p>Allows for registering classes one by one using {@link #register(Class...)}
 * as well as for classpath scanning using {@link #scan(String...)}.
 *
 * <p>In case of multiple {@code @Configuration} classes, {@link Bean @Bean} methods
 * defined in later classes will override those defined in earlier classes. This can
 * be leveraged to deliberately override certain bean definitions via an extra
 * {@code @Configuration} class.
 *
 * <p>See {@link Configuration @Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	private final AnnotatedBeanDefinitionReader reader;

	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * 默认构造方法：
	 * 1. 实例化reader属性(AnnotatedBeanDefinitionReader)，并将this传入AnnotatedBeanDefinitionReader(this)， this实现了BeanDefinitionRegistry，
	 * 2. 实例化scanner属性，
	 *
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigApplicationContext() {
		// 将this传入构造方法中，reader中引用了该registry(BeanDefinitionRegistry), 所以也就持有了beanFactory
		this.reader = new AnnotatedBeanDefinitionReader(this);
		// 将this传入构造方法中，scanner中引用了该registry(BeanDefinitionRegistry), 所以也能获取到beanFactory
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 该类的继承关系：
	 * org.springframework.core.io.DefaultResourceLoader
	 * -- 该类中引用了classLoader，根据this对象的类加载器获取默认的类加载器
	 * 	 org.springframework.context.support.AbstractApplicationContext
	 * 	 -- 该类的默认构造方法中，会创建resourcePatternResolver=new ResourcePatternResolver()实例，(后续补充该类的作用 TODO)
	 * 	   org.springframework.context.support.GenericApplicationContext
	 * 	   -- 该类中初始化了DefaultListableBeanFactory，并赋值在beanFactory属性上，最重要的一个属性，所有的类定义信息全部保存在该对象中
	 * 	   -- 该beanFactory中包括registerBeanDefinition等一系列重要的BeanDefinition注册方法等
	 *       org.springframework.context.annotation.AnnotationConfigApplicationContext
	 *
	 * 注解式配置的启动类，该构造方法会调用默认的构造AnnotationConfigApplicationContext()，默认构造方法中会初始化很多变量，详情见默认构造方法，包括以下三个步骤"
	 * 1. this(): 调用默认构造方法，该构造方法会初始化很多变量，
	 *    1. 包括最重要的DefaultListableBeanFactory; DefaultListableBeanFactory是在其父类(GenericApplicationContext)的构造方法中创建的，父类还引用了类加载器，是一个AppClassLoader
	 *    2. reader：用于注册引用中的beanDefinition， 调用该方法时，会首先注册spring内嵌的beanDefinition(6个)，在AnnotationConfigUtils.registerAnnotationConfigProcessors
	 *    3. scanner：
	 * 2. register(): 注册spring内部的beanDefinition
	 * 	  	注册配置类，该方法最终会调用
	 * 		reader.register -->
	 * 		然后调用registry.register -->
	 * 		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry) -->
	 * 		registry.registryBean()
	 * 		最终效果就是在beanFactory的beanDefinitionMap中放入name -> BeanDefinition的键值对
	 * 		registry就是this, this的父类中引用了beanFactory
	 * 3. refresh(): 刷新context， 该方法包括12个大的步骤，每个步骤都非常重要，完成spring容器初始化
	 *
	 *
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given component classes and automatically refreshing the context.
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		this();
		register(componentClasses);
		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for components
	 * in the given packages, registering bean definitions for those components,
	 * and automatically refreshing the context.
	 * @param basePackages the packages to scan for component classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		scan(basePackages);
		refresh();
	}


	/**
	 * Propagate the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * Set the {@link ScopeMetadataResolver} to use for registered component classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * Register one or more component classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public void register(Class<?>... componentClasses) {
		Assert.notEmpty(componentClasses, "At least one component class must be specified");
		this.reader.register(componentClasses);
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param basePackages the packages to scan for component classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.scanner.scan(basePackages);
	}


	//---------------------------------------------------------------------
	// Adapt superclass registerBean calls to AnnotatedBeanDefinitionReader
	//---------------------------------------------------------------------

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
