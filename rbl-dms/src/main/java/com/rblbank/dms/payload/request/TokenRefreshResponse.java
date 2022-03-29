package com.rblbank.dms.payload.request;

public class TokenRefreshResponse {
	  private String accessToken;
	  private String refreshToken;
	  public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	private String tokenType = "Bearer";

	  public TokenRefreshResponse(String accessToken, String refreshToken) {
	    this.accessToken = accessToken;
	    this.refreshToken = refreshToken;
	  }

	  // getters and setters
	}