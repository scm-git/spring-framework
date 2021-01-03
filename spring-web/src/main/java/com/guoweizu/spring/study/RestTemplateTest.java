package com.guoweizu.spring.study;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class RestTemplateTest {

	public static void main(String[] args) {
		RestTemplate restTemplate = new RestTemplate();
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			ResponseEntity<String> forEntity = restTemplate.getForEntity("http://www.baidu.com/s", String.class, map.put("wd", "w_xd" + i));
			System.out.println("forEntity:" + forEntity);
		}
	}

}
