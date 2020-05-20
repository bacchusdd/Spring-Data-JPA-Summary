package com.study.datajpa.controller;

import javax.annotation.PostConstruct;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.datajpa.dto.MemberDto;
import com.study.datajpa.entity.Member;
import com.study.datajpa.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberRepository memberRepository;
	
	@GetMapping("/members")
	public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable){
		Page<Member> page = memberRepository.findAll(pageable);
		return page.map(m-> new MemberDto(m.getId(), m.getUsername(),null));
	}
	
	
	@PostConstruct
	public void init() {
		for(int i=0; i<100;i++) {
			memberRepository.save(new Member("user" + i, i, null));
		}
	}
}
