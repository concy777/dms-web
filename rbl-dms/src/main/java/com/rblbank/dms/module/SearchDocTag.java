package com.rblbank.dms.module;

import lombok.Data;

@Data
public class SearchDocTag {
	
	
	
	    public String docFolder;
	    public String cifId;
	    public String ucic;
	    public String sourceSystem;
	    public String referenceNumber;
	    
	    public SearchReqDateofAccOpening dateOfAccOpening;
	    public String accountNumber;
	    public SearchReqLastKycCheck lastKycCheck;
	

}
