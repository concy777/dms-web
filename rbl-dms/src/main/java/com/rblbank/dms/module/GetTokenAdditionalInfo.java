package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GetTokenAdditionalInfo {
	
	@JsonProperty("SessionId") 
    public String sessionId;

}
