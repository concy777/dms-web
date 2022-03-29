package com.rblbank.dms.module;

import java.util.List;

import lombok.Data;

@Data
public class SearchResBody {
	
	 public String pageNumber;
	    public int pageSize;
	    public SearchDocTag documentTags;
	   public int totalRecords;
	    public List<SearchDocDetails> documentDetails;
	    public String documentId;

}
