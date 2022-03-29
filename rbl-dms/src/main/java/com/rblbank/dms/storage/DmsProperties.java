package com.rblbank.dms.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "dms")
public class DmsProperties {
	
	private String instance;
	private String uatUrl;
	private String prodUrl;
	private String corpid;
	private String tranid;
	
	

}
