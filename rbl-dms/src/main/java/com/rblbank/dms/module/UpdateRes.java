package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class UpdateRes {
	
	@JsonProperty("ResponseHeader") 
    public ResponseHeader header;
    @JsonProperty("ResponseBody") 
    public UpdateReqBody body;
    @JsonProperty("Status") 
    public SearchResStatus status;

}
