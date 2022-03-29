package com.rblbank.dms.dto;

import lombok.Data;

@Data
public class FileUploadDTO {
	private Integer upload_id;
	private String fileName;
	private String fileType;
	private String fileSize;
	private String accNo;
	private String cifId;
	private String ucic;
	private String source;
	private String refNo;
	private String dateOfOpening;
	private String isKYC;
	private String filePath;
	private String iserror;
	private String error;
	private String status;
	private String docfolder;
	private String doctype;
	private String transId;

}
