package com.rblbank.dms.module;



import lombok.Data;

@Data
public class GenerateTokenResponseBody {
	public String access_token;
	public String expires_in;
	public String token_type;

}
