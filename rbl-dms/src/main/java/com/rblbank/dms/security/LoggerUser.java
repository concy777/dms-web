package com.rblbank.dms.security;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.rblbank.dms.utils.HttpRequestResponseUtils;

@Component
public class LoggerUser implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(LoggerUser.class);
	 
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		final String ip = HttpRequestResponseUtils.getClientIpAddress();
		final String url = HttpRequestResponseUtils.getRequestUrl();
		final String page = HttpRequestResponseUtils.getRequestUri();
		final String refererPage = HttpRequestResponseUtils.getRefererPage();
		final String queryString = HttpRequestResponseUtils.getPageQueryString();
		final String userAgent = HttpRequestResponseUtils.getUserAgent();
		final String requestMethod = HttpRequestResponseUtils.getRequestMethod();
		final LocalDateTime timestamp = LocalDateTime.now();

		
		System.out.println("User-IP:"+ip);
		System.out.println("User-requestMethod:"+requestMethod);
		System.out.println("User-referer:"+refererPage);
		
		logger.info("User-IP:"+ip);
		logger.info("User-url:"+url);
		logger.info("User-page:"+page);
		logger.info("User-referer:"+refererPage);
		logger.info("User-queryString:"+queryString);
		logger.info("User-userAgent:"+userAgent);
		logger.info("User-requestMethod:"+requestMethod);
		logger.info("Timestamp:"+timestamp);
		
		

		return true;
	}

}
