package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SearchRes {
	
	
	    @JsonProperty("ResponseHeader") 
	    public ResponseHeader header;
	    @JsonProperty("ResponseBody") 
	    public SearchResBody body;
	    @JsonProperty("Status") 
	    public SearchResStatus status;
	

}
