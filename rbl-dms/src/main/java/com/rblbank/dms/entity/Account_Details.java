package com.rblbank.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Account_details")
public class Account_Details{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name = "file_name")
	private String fileName;
	@Column(name = "channel_id")
	private String channel_id;
	@Column(name = "cif_id")
	private String cif_id;
	@Column(name = "source")
	private String source;
	@Column(name = "ucic")
	private String ucic;
	@Column(name = "doc_type")
	private String docType;
	@Column(name = "document")
	private String document;
	@Column(name = "case_type")
	private String case_type;
	@Column(name = "business")
	private String business;
	@Column(name = "account_number")
	private String account_number;
	@Column(name = "rep_name")
	private String rep_name;
	@Column(name = "doc_date")
	private String docDate;
	@Column(name = "account_type")
	private String account_type;
	@Column(name = "case_number")
	private String case_number;
	private String file_path;
	private String error;
	private String status;
	private String iserror;
	@Column(name = "doc_folder")
	private String doc_Folder;
	private String reference_Number;
	private String kyc_Id;
	private String validity;
	private String name;
	private boolean kyc;
    private String transId;
	

}
