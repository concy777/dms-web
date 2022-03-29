package com.rblbank.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FileUpload extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer upload_id;
	@Column(name = "file_Name")
	private String fileName;
	@Column(name = "file_Type")
	private String fileType;
	@Column(name = "file_Size")
	private String fileSize;
	@Column(name = "accNo")
	private String accNo;
	@Column(name = "cifId")
	private String cifId;
	private String ucic;
	private String source;
	private String refNo;
	private String dateOfOpening;
	private String isKYC;
	@Column(name = "file_Path")
	private String filePath;
	private String iserror;
	private String error;
	private String status;
	@Column(name = "doc_folder")
	private String docfolder;
	@Column(name = "doc_type")
	private String doctype;

}
