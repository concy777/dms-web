package com.rblbank.dms.service;


import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rblbank.dms.dao.DmsUser;
import com.rblbank.dms.entity.AppUser;
import com.rblbank.dms.entity.DmsAppLoginUser;
import com.rblbank.dms.entity.DmsAppUser;

public interface DmsUserService {

	DmsUser loadUser(String username) throws UsernameNotFoundException;

	AppUser loadAppUser(String userName) throws UsernameNotFoundException;

	DmsAppUser getUserById(String id);
	
	void insertUser(DmsAppLoginUser dmsUser,String instance) throws Exception;

}
