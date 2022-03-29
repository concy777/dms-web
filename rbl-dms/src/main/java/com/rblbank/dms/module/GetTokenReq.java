package com.rblbank.dms.module;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class GetTokenReq {
	@JsonProperty("RequestHeader")
	public RequestHeader requestHeader;
	@JsonProperty("RequestBody")
	public GetTokenRequestBodyRoot requestBody;

}
