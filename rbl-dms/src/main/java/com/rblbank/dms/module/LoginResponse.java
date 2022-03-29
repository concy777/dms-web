package com.rblbank.dms.module;

import lombok.Data;

@Data
public class LoginResponse {
	
	public Details Details;
	public String Status;
	public String Username;
	public String Email;
	public String displayname;
	public String Mobile;
	public String Telephone;
	public String Location;
	public String Group;

}
