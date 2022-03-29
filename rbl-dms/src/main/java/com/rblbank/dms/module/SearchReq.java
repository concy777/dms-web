package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class SearchReq {
	
	
	    @JsonProperty("RequestHeader") 
	    public RequestHeader header;
	    @JsonProperty("RequestBody") 
	    public SearchReqBody body;
	

}
