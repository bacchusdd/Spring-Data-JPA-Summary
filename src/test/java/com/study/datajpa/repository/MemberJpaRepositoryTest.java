package com.study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.study.datajpa.entity.Member;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {
	
	@Autowired MemberJpaRepository memberJpaRepository;
	
	@Test
	public void testMember() {
		Member member = new Member("memberA");
		Member savedMember = memberJpaRepository.save(member);
		Member findMember = memberJpaRepository.find(savedMember.getId());
		
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}
	
	@Test
	public void 기본_CRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);
		
		//단건 조회 검증
		Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
		Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
		assertThat(member1).isEqualTo(findMember1);
		assertThat(member2).isEqualTo(findMember2);
		
		//리스트 조회 검증
		List<Member> all = memberJpaRepository.findAll();
		assertThat(all.size()).isEqualTo(2);
		
		//카운트 검증
		long count = memberJpaRepository.count();
		assertThat(count).isEqualTo(2);
		
		//삭제 검증
		memberJpaRepository.delete(findMember1);
		memberJpaRepository.delete(findMember2);
		count = memberJpaRepository.count();
		assertThat(count).isEqualTo(0);
	}
	
	@Test
	public void JPQL_메서드_테스트() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberJpaRepository.save(m1);
		memberJpaRepository.save(m2);
		
		List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
		
		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}
	
	@Test
	public void paging() {
		memberJpaRepository.save(new Member("member1", 10, null));
		memberJpaRepository.save(new Member("member2", 10, null));
		memberJpaRepository.save(new Member("member3", 10, null));
		memberJpaRepository.save(new Member("member4", 10, null));
		memberJpaRepository.save(new Member("member5", 10, null));
		
		int age = 10;
		int offset = 0;
		int limit = 3;
		
		//when
		List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
		long totalCount = memberJpaRepository.totalCount(age);
		
		//then
		assertThat(members.size()).isEqualTo(3);
		assertThat(totalCount).isEqualTo(5);
	}
	
	@Test
	public void 벌크_수정() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		Member m3 = new Member("AAA", 30, null);
		
		memberJpaRepository.save(m1);
		memberJpaRepository.save(m2);
		memberJpaRepository.save(m3);
		
		
		int successCount = memberJpaRepository.bulkUpdate(20);
		
		assertThat(successCount).isEqualTo(2);
	}
}
