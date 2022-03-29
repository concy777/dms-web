package com.rblbank.dms.module;

import lombok.Data;

@Data
public class Details {
	
	public String UserId;
	public String ChlId;
	public String DeviceFamily;
	public String DeviceFormat;
	public int OperationId;
	public String LoginPwd;
	public double ClientAPIVer;
	public String SessionId;
	public int TransSeq;

}
