package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GenerateTokenResponse {
	@JsonProperty("ResponseHeader")
	public GetTokenResponseHeader responseHeader;
	@JsonProperty("ResponseBody")
	public GenerateTokenResponseRootBody responseBody;
	@JsonProperty("Status")
	public SearchResStatus status;
	

}
