package com.rblbank.dms.dao;

 
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rblbank.dms.entity.AppUser;
import com.rblbank.dms.entity.DmsAppUser;
 
@Repository
@Transactional
public class AppUserDAO {
 
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
 
    public AppUser findUserAccount(String userName) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.userId = "+userName;
 
            Query query = entityManager.createQuery(sql, AppUser.class);
           // query.setParameter("username", userName);
 
            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
        	e.printStackTrace();
            return null;
        }
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
	public DmsAppUser getUserById(String id) {
		// TODO Auto-generated method stub

		String SELECT_BY_ID_QUERY = "SELECT * from dms_app_user where user_name = ?";
   	 
		DmsAppUser dmsappuser=	jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY, getMap(), id);
		if (dmsappuser == null) {
			
			//appUserDAO.insertWithQuery(appUser);
			
			System.out.println("User not found! " + id);
			throw new UsernameNotFoundException("User " + id + " was not found in the database");
		}
		
		System.out.println("User--"+dmsappuser.getUser_Name() + "____"+dmsappuser.getUser_id() );
		return dmsappuser;
		
	}

    
    public DmsAppUser findDMSUserAccount(String userName) {
        try {
            String sql = "Select e from " + DmsAppUser.class.getName() + " e " //
                    + " Where e.user_name = '"+userName+"'";
 
            Query query = entityManager.createQuery(sql, DmsAppUser.class);
           // query.setParameter("username", userName);
 
            return (DmsAppUser) query.getSingleResult();
        } catch (NoResultException e) {
        	e.printStackTrace();
            return null;
        }
    }
    
    @Transactional
    public void insertWithQuery(AppUser customer)
    {
        entityManager.createNativeQuery("INSERT INTO App_User "
                + " (User_Id, User_name, Encryted_Password, Enabled) "
                + " VALUES (?, ?, ?, ?)")
        .setParameter(1, customer.getUserId())
        .setParameter(2, customer.getUserName())
        .setParameter(3, customer.getEncrytedPassword())
        .setParameter(4, "Y")
        .executeUpdate();
    }
 
}

