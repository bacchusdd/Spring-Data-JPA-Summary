package com.study.datajpa.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.study.datajpa.entity.Member;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom{

	private final EntityManager em;
	private final JdbcTemplate template;
	
	public List<Member> findMemberCustom() {
		return em.createQuery("select m from Member m", Member.class)
		.getResultList();
	}
}
