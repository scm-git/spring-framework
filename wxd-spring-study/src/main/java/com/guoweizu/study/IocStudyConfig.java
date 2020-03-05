package com.guoweizu.study;

import com.test.Bar2Service;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Configuration注解的类会被解析为一个ConfigurationClass对象
 *
 */

@ComponentScan(basePackages = "com.guoweizu")
@Configuration
@Import(value = Bar2Service.class)
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class IocStudyConfig {

	@Bean(name = "bean2")
	public Set<String> set() {
		System.out.println("bean2");
		return new HashSet<>();
	}

	@Bean(name = "bean2")
	public List<String> list() {
		System.out.println("bean3");
		return new ArrayList<>();
	}

	@Bean
	public DataSourceTransactionManager transactionManager(DataSource datasource) {
		return new DataSourceTransactionManager(datasource);
	}

	@Bean
	public DataSource dataSource() {
		DataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost:3306/myapp", "root", "123456", true);
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Scheduled(fixedRate = 30000)
	@Scheduled(fixedRate = 50000)
	public void doScheduleTask() {
		System.out.println("测试定时任务两个注解。。。。");
	}
}
