package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AddDocTags {
	
	public String docFolder;
	@JsonProperty("accountNumber") 
    public String accNo;
    public String dateOfAccOpening;
    public String referenceNumber;
    public String cifId;
    public String ucic;
    public String documentType;
    public String sourceSystem;
    public String kycId;
    public String validity;
    public String name;
    @JsonProperty("isValid") 
    public boolean kyc;
    public String lastKycCheck;
    public String placeHolder1;
    public String placeHolder2;
    public String placeHolder3;
    public String identificationNumber;

}
