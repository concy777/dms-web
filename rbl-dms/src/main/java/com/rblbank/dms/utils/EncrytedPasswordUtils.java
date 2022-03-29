package com.rblbank.dms.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncrytedPasswordUtils {

// Encryte Password with BCryptPasswordEncoder
	public static String encrytePassword(String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(password);
	}

	public static String convertToDateFormat(String date) {
		System.out.println("Date:::"+date);
		String formating="";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dateog = formatter.parse(date);
			 formating = formatter.format(date);
			System.out.println(formating);
			//return formating;
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String format = formatter.format(date);
	//	System.out.println(format);
		return formating;
	}
	
	
	public static long getTodayPassedSeconds() {
		ZonedDateTime nowZoned = ZonedDateTime.now();
		Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
		Duration duration = Duration.between(midnight, Instant.now());
		long seconds = duration.getSeconds();
		return seconds;
	}
	
	  public static void main(String[] args) { 
		  String password = "rbl@1234"; 
		  String encrytedPassword = encrytePassword(password);
		  
		  System.out.println(getTodayPassedSeconds());
	  
	  
	 
	  //System.out.println("Encryted Password: " + convertToDateFormat("17/01/21")); 
	  }
	 
	 
	 

}