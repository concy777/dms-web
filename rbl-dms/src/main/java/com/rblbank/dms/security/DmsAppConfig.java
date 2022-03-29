package com.rblbank.dms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class DmsAppConfig implements WebMvcConfigurer {

	@Autowired
	private LoggerUser visitorLogger;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(visitorLogger);
	}
}
