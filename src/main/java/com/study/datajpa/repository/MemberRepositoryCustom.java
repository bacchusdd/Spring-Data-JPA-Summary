package com.study.datajpa.repository;

import java.util.List;

import com.study.datajpa.entity.Member;

public interface MemberRepositoryCustom {
	
	List<Member> findMemberCustom();
}
