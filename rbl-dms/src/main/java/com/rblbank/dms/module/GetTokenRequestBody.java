package com.rblbank.dms.module;

import lombok.Data;

@Data
public class GetTokenRequestBody {
	public String clientId;
	public String clientSecret;

}
