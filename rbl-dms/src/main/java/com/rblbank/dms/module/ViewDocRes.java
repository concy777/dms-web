package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ViewDocRes {
	
	@JsonProperty("ResponseHeader") 
    public RequestHeader header;
    @JsonProperty("ResponseBody") 
    public ViewResBody body;
    @JsonProperty("Status") 
    public SearchResStatus status;

}
