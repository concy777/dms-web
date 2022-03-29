package com.rblbank.dms.module;

import lombok.Data;

@Data
public class AddReqBody {
	
	public AddDocumentDetails documentDetails;
	public AddDocTags documentTags;
	public String document;

}
