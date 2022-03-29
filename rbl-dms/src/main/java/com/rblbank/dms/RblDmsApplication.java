package com.rblbank.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.rblbank.dms.storage.StorageProperties;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;




@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class})
@EnableEncryptableProperties
public class RblDmsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(RblDmsApplication.class, args);
	}
 
}
