package com.rblbank.dms.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dms_user_login_history")
public class DmsAppLoginUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer user_id;
	public String user_Name;
	public String ip_address;
	public String user_role;
	public String created_by;
	public Date created_on;


}

