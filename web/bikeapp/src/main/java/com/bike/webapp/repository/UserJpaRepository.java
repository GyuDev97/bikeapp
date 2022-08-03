package com.bike.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bike.webapp.dto.*;
@Repository
public interface UserJpaRepository extends JpaRepository<Login, String>{
	public Login findByUserId(String userId);
}