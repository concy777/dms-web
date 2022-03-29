package com.rblbank.dms.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rblbank.dms.entity.Account_Details;

public interface AccountDetailRepository extends JpaRepository<Account_Details, Integer> {

}
