package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class SearchResStatus {
	
	
	    @JsonProperty("StatusCode") 
	    public String statusCode;
	    @JsonProperty("ErrorCode") 
	    public String errorCode;
	    @JsonProperty("ErrorMessage") 
	    public String errorMessage;
	    @JsonProperty("DisplayText") 
	    public String displayText;
	

}
