/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link EnableAspectJAutoProxy} 中的注解@Import就是导入这个类来启用AOP的
 *
 * BeanDefinition的注册过程需要详细查看refresh的第五步, 此处做简要说明：
 * 1. ConfigurationPostProcessor会解析所有的@Import注解，
 *    如果其value属于ImportBeanDefinitionRegistrar(本类就是)，则放入ConfigurationClass的importBeanDefinitionRegistrars字段中
 *    其他类型会放入其他ConfigurationClass的其他属性中，详见 {@link ConfigurationClass}
 * 2. 解析之后会调用registrar.registerBeanDefinitions(importingClassMetadata, registry, importBeanNameGenerator)
 *    其中调用者registrar就是importBeanDefinitionRegistrars字段中的元素，就是循环的调用这些registrar的方法
 * 3. 而对于AspectJ的这个类AspectJAutoProxyRegistrar，调用第二步中的方法时(该类继承父类的)，会调用到当前这个类的registerBeanDefinitions(importingClassMetadata, registry)
 *    所有查看该类的该方法，可以看到很重要的一步：
 *    AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry)，查看这个方法会看到：
 *    该方法注册了一个BeanDefinition: AnnotationAwareAspectJAutoProxyCreator
 * 4. {@link org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator} 这个类会在refresh的第十一步用到
 *
 *
 * Registers an {@link org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 * AnnotationAwareAspectJAutoProxyCreator} against the current {@link BeanDefinitionRegistry}
 * as appropriate based on a given @{@link EnableAspectJAutoProxy} annotation.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAspectJAutoProxy
 */
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		// 注册AnnotationAwareAspectJAutoProxyCreator BeanDefinition
		// name： org.springframework.aop.config.internalAutoProxyCreator
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				// 给内置的org.springframework.aop.config.internalAutoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) BeanDefinition增加属性：proxyTargetClass=true
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				// 给内置的org.springframework.aop.config.internalAutoProxyCreator(AnnotationAwareAspectJAutoProxyCreator) BeanDefinition增加属性：exposeProxy=true
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}
