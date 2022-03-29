package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AddResBody {

	@JsonProperty("DocumentDetails") 
	public AddResDocumentDetails documentDetails;
}
