/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	/**
	 * 处理BeanFactoryPostProcessor, 需要处理两处：
	 * 一处是通过入参传入的beanFactoryPostProcessors(也就是AbstractApplicationContext中的beanFactoryPostProcessors字段)
	 * 另一处是通过beanFactory中获取到的，已经注册的BeanDefinitionMap中获取的
	 *
	 * 处理逻辑：
	 * 一. 对入参传入的beanFactoryPostProcessors进行分组：
	 * 	1. registryProcessors： 将instanceof BeanDefinitionRegistryPostProcessor的 放入registryProcessors中
	 *     BeanDefinitionRegistryPostProcessor是BeanFactoryPostProcessor的子类，
	 *     筛选出来是为了做额外的处理：registryProcessor.postProcessBeanDefinitionRegistry(registry)
	 *     后面从beanFactory中获取的BeanDefinitionRegistryPostProcessor会调用invokeBeanDefinitionRegistryPostProcessors方法，
	 *     其内部实现也只是在for循环中一次调用registryProcessor.postProcessBeanDefinitionRegistry(registry)
	 * 	2. regularPostProcessors： 其他的放入regularPostProcessors中
	 *
	 * 二. 最最最重要的步骤：加载BeanDefinition到beanFactory.beanDefinitionMap中，也就是说，这步完成之后，就加载到了应用中的BeanDefinition
	 * 通过beanFactory获取已注册的BeanDefinitionRegistryPostProcessor进行如下处理：
	 *  1. 获取属于PriorityOrdered类型的bean，然后排序，并调用invokeBeanDefinitionRegistryPostProcessors方法
	 *  2. 获取属于Ordered类型的bean且不属于PriorityOrdered的，然后排序，并调用invokeBeanDefinitionRegistryPostProcessors
	 *  3. 获取不属于前面两种类型的bean，然后排序，并调用invokeBeanDefinitionRegistryPostProcessors
	 *  将上面三步获取的bean全部放入registryProcessors中， 分步处理就是为了保证优先级，实际都是调用registryProcessor.postProcessBeanDefinitionRegistry(registry)
	 *  但是第3步中获取时用了while(true)语句，可能是因为处理期间可能增加新的BeanDefinitionRegistryPostProcessor到beanFactory中 TODO
	 *
	 * 三. 分别调用：
	 *    invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
	 * 	  invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
	 *    也就是说：对于BeanDefinitionRegistryPostProcessor，需要多一步registryProcessor.postProcessBeanDefinitionRegistry(registry)调用，且需要保证先后顺序
	 *    然后对所有的BeanFactoryPostProcessor都需要调用postProcessor.postProcessBeanFactory(beanFactory);
	 *
	 *    此时已经处理了入参传入的和beanFactory中的BeanDefinitionRegistryPostProcessor，以及入参传入的BeanFactoryPostProcessor
	 *    还有beanFactory中的BeanFactoryPostProcessor未处理，接下来就需要处理这部分：
	 *
	 * 四. 处理beanFactory中的BeanFactoryPostProcessor
	 *     同样需要对这些beanFactoryPostProcessor进行分批处理，分批逻辑和第二步类似:
	 *     1. 筛选出PriorityOrdered类型的，然后排序，并调用invokeBeanFactoryPostProcessors
	 *     2. 筛选出Ordered类型的，然后排序，并调用invokeBeanFactoryPostProcessors
	 *     3. 筛选出其他的bean，然后排序，并调用invokeBeanFactoryPostProcessors
	 *     调用invokeBeanFactoryPostProcessors方法最终也就是调用postProcessor.postProcessBeanFactory(beanFactory);
	 *     对入参传入的BeanFactoryPostProcessor(包括BeanDefinitionRegistryPostProcessor)不需要处理优先级，所以可以直接调用postProcess方法
	 *
	 * 五. 清除缓存，因为上述步骤可能会修改属性，比如属性注入，
	 *    主要清除如下属性：beanFactory.allBeanNamesByType, beanFactory.singletonBeanNamesByType
	 *
	 * 有两个名为invokeBeanFactoryPostProcessors的方法，一个是处理所有BeanFactoryPostProcessor的，给外部调用的；
	 * 另一个是private的，用于处理非BeanDefinitionRegistryPostProcessor的BeanFactoryPostProcessor
	 *
	 * 总结一下：
	 * 1. 容器中可能有很多BeanDefinitionRegistryPostProcessor，将这些类型的bean按优先级分组，然后每个组分别排序，再执行postProcessBeanDefinitionRegistry方法
	 *    执行完成就已经加载了应用程序中的BeanDefinition，然后执行继承自父类BeanFactoryPostProcessor的postProcessBeanFactory方法(这部分没有优先级)
	 * 2. 再分组排序执行只实现了BeanFactoryPostProcessor接口的类的postProcessBeanFactory
	 * 3. 优先级只对spring容器管理的postProcessor起作用，对外部直接传入的（入参传入的）不生效
	 *
	 *
	 *
	 * @param beanFactory
	 * @param beanFactoryPostProcessors AbstractApplicationContext实例中的<BeanFactoryPostProcessor> beanFactoryPostProcessors属性
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			// BeanDefinitionRegistryPostProcessor是BeanFactoryPostProcessor子类，将他们分组
			// 将属于子类BeanDefinitionRegistryPostProcessor的调用其postProcessBeanDefinitionRegistry方法，并放入registryProcessors列表中
			// 其他的普通BFPP放入regularPostProcessors列表中，不调用其方法（会放在后面一起调用）
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					// 扫描scanner策略指定的组件，并加载BeanDefinition
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// 第1步：筛选PriorityOrdered类型的bean
			// 此时至少能获取到一个ConfigureClassPostProcessor(是BeanDefinitionRegistryPostProcessor的子类), 它是在初始化reader时创建的6个内置BeanDefinition中的一个
			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					/**
					 * 添加到当前将要处理的postProcessor, bean还没有实例化，为什么此处可以getBean()? --
					 * 因为getBean的过程就是bean的创建过程：如果bean已存在则从单例缓存池中获取，如果不存在则创建bean,并且放入单例缓存池(scope为singleton的情况)
					 *
					 * 此处就会创建{@link org.springframework.context.annotation.ConfigurationClassPostProcessor}的实例(ApplicationContext构造方法中注册的6个BeanDefinition中的一个)
					 * 创建了实例之后，就能调用其postProcessBeanDefinitionRegistry方法
					 */
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 添加到已处理列表
					processedBeans.add(ppName);
				}
			}
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 调用该方法进入真正的处理逻辑，只传入了本次筛选出的BeanFactoryPostProcessor(实现了PriorityOrdered接口的)
			 *
			 * 此处会调用{@link org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)}
			 * ConfigurationClassPostProcessor就是在AnnotationConfigApplicationContext的默认构造方法中注册的一个BDRPP
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			// 清理，准备处理Ordered类型的
			currentRegistryProcessors.clear();

			// 第2步：
			// 处理Ordered类型的，同时需要满足 !processedBeans.contains(ppName)，即第1步未处理该bean,
			// 因为 PriorityOrdered extends Ordered，所以需要排除掉已处理过的;
			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// 第3步： 处理剩下的其他BeanDefinitionRegistryPostProcessor
			// 此处使用while (true)，是因为处理过程中，可能增加新的BeanDefinitionRegistryPostProcessor吗？ TODO
			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		} else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	/**
	 * 注册bean的后置处理器
	 * @param beanFactory
	 * @param applicationContext
	 */
	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		/**
		 * 此处获取所有类型为BeanPostProcessor的BeanDefinition, 然后根据优先级放入beanFactory.beanPostProcessors中
		 *
		 */
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		/**
		 * 此时beanFactory中有3-4个BeanPostProcessor，
		 *  refresh第3步和第5步分别添加进去的，详细点击如下链接：
		 * {@link org.springframework.beans.factory.support.AbstractBeanFactory#beanPostProcessors }
		 *
		 * beanProcessorTargetCount=beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length
		 * 1. beanFactory.getBeanPostProcessorCount()： 已添加到beanFactory.beanPostProcessors集合中的
		 * 2. 中间的1表示下一步马上要添加到beanPostProcessors中的BeanPostProcessorChecker
		 * 3. postProcessorNames.length 表示从beanFactory中获取到的bean，即将在后面添加的(按优先级添加)
		 */
		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		/**
		 * 从beanFactory中获取所有的BeanPostProcessor类型的bean
		 * 对BeanFactoryProcessor根据PriorityOrdered, Ordered及其他三个优先级分组（和BeanFactoryPostProcessor一样）：
		 * 然后按顺序添加到beanFactory.beanPostProcessors（一个List）中，最后添加spring内置的BeanFactory
		 * PriorityOrdered > Ordered > 其他 > spring内置
		 */
		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		/**
		 * AOP的AnnotationAwareAspectJAutoProxyCreator就会在此处添加到beanFactory.beanPostProcessors中，因为它只实现了@Ordered接口
		 */
		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 *
	 * 遍历postProcessors，并逐个调用postProcessBeanDefinitionRegistry方法
	 * postProcessors是根据优先级排好序的
	 * registry就是beanFactory
	 *
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * 调用postProcessor.postProcessBeanFactory,
	 * 干方法会添加一个BeanPostProcessor
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
