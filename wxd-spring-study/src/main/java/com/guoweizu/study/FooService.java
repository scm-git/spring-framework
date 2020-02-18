package com.guoweizu.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = {"classpath: foo.properties"})
public class FooService {
	@Value("${foo.name}")
	private String name;
	@Value("${foo.value}")
	private String value;
	@Value("${foo.age}")
	private Integer age;

	@Autowired
	private Object bean1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
}
