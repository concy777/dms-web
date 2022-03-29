package com.rblbank.dms.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.rblbank.dms.dao.AppRoleDAO;
import com.rblbank.dms.dao.AppUserDAO;
import com.rblbank.dms.dao.DmsAppUserRepository;
import com.rblbank.dms.dao.DmsUser;
import com.rblbank.dms.entity.Account_Details;
import com.rblbank.dms.entity.AppUser;
import com.rblbank.dms.entity.DmsAppLoginUser;
import com.rblbank.dms.entity.DmsAppUser;
import com.rblbank.dms.entity.FileUpload;
import com.rblbank.dms.exception.ResourceNotFoundException;
import com.rblbank.dms.service.DmsUserService;

@Service
public class DmsUserServiceImpl implements DmsUserService {
	
	@Autowired
	private AppUserDAO appUserDAO;

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private AppRoleDAO appRoleDAO;

	
	@Autowired
	private DmsAppUserRepository dmsRepository;
	
	
	@Override
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
	
	@Override
	public AppUser loadAppUser(String userName) throws UsernameNotFoundException {
		AppUser appUser = this.appUserDAO.findUserAccount(userName);

		if (appUser == null) {
			
			//appUserDAO.insertWithQuery(appUser);
			
			System.out.println("User not found! " + userName);
			throw new UsernameNotFoundException("User " + userName + " was not found in the database");
		}

		System.out.println("Found User: " + appUser);

		return appUser;
	}
	private RowMapper<DmsAppUser> getMap(){
		  // Lambda block
		  RowMapper<DmsAppUser> accMap = (rs, rowNum) -> {
			  DmsAppUser acc = new DmsAppUser();
		      acc.setUser_id(rs.getInt("user_id"));
		      acc.setUser_Name(rs.getString("user_name"));
		      acc.setUser_role(rs.getString("user_role"));
		      acc.setIsActive(rs.getString("isActive"));
		      return acc;
		  };
		  return accMap;
	}
	
public void insertUser(DmsAppLoginUser dmsUser,String instance) throws Exception {
		
		int row;
		try {
			SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
			dataSource.setDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
			
			if(instance.equalsIgnoreCase("local")) {
				
				dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=dms");
				dataSource.setUsername("sa");
				dataSource.setPassword("Sara@2020");
			}
			
			else {
				
				dataSource.setUrl("jdbc:sqlserver://10.80.74.21:21455;databaseName=CRMNext_UAT1");
				dataSource.setUsername("crmnext_rbl");
				dataSource.setPassword("rbl@1234");
			}
      
			
			String sql = "INSERT INTO dms_user_login_history " +
			        "(USER_ID, USER_NAME, user_role,IP_ADDRESS,created_by,created_ON) VALUES (?,?,?,?,?,getdate())";
			             
			JdbcTemplate   jdbcTemplate = new JdbcTemplate(dataSource);
			            
			  row = jdbcTemplate.update(sql, new Object[] { dmsUser.getUser_id(),dmsUser.getUser_Name(),dmsUser.getUser_role(),dmsUser.getIp_address(),dmsUser.getUser_id()
			    });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
			
		}
	      
	      System.out.println("Rows added--"+row);
	}

	
	@Override
	public DmsAppUser getUserById(String id) {
		// TODO Auto-generated method stub

		String SELECT_BY_ID_QUERY = "SELECT * from dms_app_user where user_id = ?";
   	 
		DmsAppUser dmsappuser=	jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY, getMap(), id);
		if (dmsappuser == null) {
			
			//appUserDAO.insertWithQuery(appUser);
			
			System.out.println("User not found! " + id);
			throw new UsernameNotFoundException("User " + id + " was not found in the database");
		}
		
		System.out.println("User--"+dmsappuser.getUser_Name() + ""+dmsappuser.getUser_id() );
		return dmsappuser;
		
	}

}
