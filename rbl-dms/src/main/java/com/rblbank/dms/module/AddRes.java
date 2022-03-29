package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class AddRes {
	
	@JsonProperty("ResponseHeader") 
    public ResponseHeader header;
    @JsonProperty("ResponseBody") 
    public AddResBody body;
    @JsonProperty("Status") 
    public SearchResStatus status;

}
