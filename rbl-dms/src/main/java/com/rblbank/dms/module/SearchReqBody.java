package com.rblbank.dms.module;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SearchReqBody {
	
	
	    public String pageNumber;
	    public int pageSize;
	    public SearchDocTag documentTags;
	  //  public int totalRecords;
	   // public List<SearchDocDetails> documentDetails;
	//    public String documentId;
	   
	

}
