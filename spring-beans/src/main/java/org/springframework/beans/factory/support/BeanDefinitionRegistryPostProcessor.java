/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;

/**
 *
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * ===================================================================================
	 * 非常非常非常重要的一个方法，目前子类实现只有 ConfigurationClassPostProcessor；
	 * 该方法会加载BeanDefinition, 也就是说，beanFactory中的beanDefinition就是这个方法加载的
	 * ===================================================================================
	 *
	 * 继承了BeanFactoryPostProcessor，执行时机在加载beanDefinition之前，
	 * 加载完成之后就开始执行下一个步骤，也就是父类BeanFactoryPostProcessor的postProcessBeanFactory方法
	 * 执行时会分优先级：根据是否实现PriorityOrdered, Ordered及其他 三个优先级分别执行
	 * 具体逻辑参见:
	 * @see org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory, List)
	 *
	 * 在AbstractApplicationContext.refresh()方法的第5步执行，具体可以参见该步骤
	 * @see org.springframework.context.support.AbstractApplicationContext
	 *
	 * 其子类：ConfigurationClassPostProcessor实现了具体的方法:
	 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
	 *
	 *
	 *
	 *
	 *
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
