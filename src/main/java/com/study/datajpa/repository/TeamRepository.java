package com.study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.datajpa.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long>{
	
}
