package com.guoweizu.study.aop.service;

public interface IUserService {

	public void printName(String name) ;


	public void printNameAndAddress(String name, Object address ) ;

	public String getName(String name) ;


	public String walk(String from, String to);

	public void goHome();
}
