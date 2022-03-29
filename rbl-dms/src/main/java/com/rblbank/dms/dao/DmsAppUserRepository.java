package com.rblbank.dms.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rblbank.dms.entity.DmsAppUser;

    @Repository
    public interface DmsAppUserRepository extends JpaRepository<DmsAppUser, Integer> {


}
