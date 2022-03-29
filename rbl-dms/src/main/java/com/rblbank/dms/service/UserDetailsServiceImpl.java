package com.rblbank.dms.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.rblbank.dms.dao.AppRoleDAO;
import com.rblbank.dms.dao.AppUserDAO;
import com.rblbank.dms.dao.DmsUser;
import com.rblbank.dms.entity.AppUser;
import com.rblbank.dms.entity.DmsAppUser;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private AppUserDAO appUserDAO;


	@Autowired
	private AppRoleDAO appRoleDAO;
	
	private final PasswordEncoder passwordEncoder;
	 
    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		DmsAppUser appUser = this.appUserDAO.getUserById(userName);

		if (appUser == null) {

			System.out.println("User not found! " + userName);
			throw new UsernameNotFoundException("User " + userName + " was not found in the database");
		}
		GrantedAuthority authority = new SimpleGrantedAuthority(appUser.getUser_role());
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		grantList.add(authority);
		//Authentication authentication = authenticationFacade.getAuthentication();
		//System.out.println(authentication.getCredentials().toString());
		UserDetails userDetails = (UserDetails) new User(appUser.getUser_Name(), passwordEncoder.encode("password"), grantList);
		return userDetails;

	}

	
	public DmsUser loadUser(String userName) throws UsernameNotFoundException {
		AppUser appUser = this.appUserDAO.findUserAccount(userName);

		if (appUser == null) {
			
			//appUserDAO.insertWithQuery(appUser);
			
			System.out.println("User not found! " + userName);
			throw new UsernameNotFoundException("User " + userName + " was not found in the database");
		}

		System.out.println("Found User: " + appUser);

// [ROLE_USER, ROLE_ADMIN,..]
		String roleNames = this.appRoleDAO.getRole(appUser.getUserId());

	
		DmsUser dmuser=new DmsUser();
		
		dmuser.setRole(roleNames);
		dmuser.setUserName(appUser.getUserName());

		return dmuser;
	}
	
	public UserDetails loadUserByUser(AppUser appuser_1) throws UsernameNotFoundException {
		AppUser appUser = this.appUserDAO.findUserAccount(appuser_1.getUserName());

		if (appUser == null) {
			
			appUserDAO.insertWithQuery(appuser_1);
			
			//System.out.println("User not found! " + userName);
			//throw new UsernameNotFoundException("User " + userName + " was not found in the database");
		}

		System.out.println("Found User: " + appUser);

// [ROLE_USER, ROLE_ADMIN,..]
		List<String> roleNames = this.appRoleDAO.getRoleNames(appUser.getUserId());

		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		if (roleNames != null) {
			for (String role : roleNames) {
// ROLE_USER, ROLE_ADMIN,..
				GrantedAuthority authority = new SimpleGrantedAuthority(role);
				grantList.add(authority);
			}
		}

		UserDetails userDetails = (UserDetails) new User(appUser.getUserName(), //
				appUser.getEncrytedPassword(), grantList);

		return userDetails;
	}
	
	
}
