package com.study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.study.datajpa.dto.MemberDto;
import com.study.datajpa.entity.Member;
import com.study.datajpa.entity.Team;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {
	
	@Autowired MemberRepository memberRepository;
	@Autowired TeamRepository teamRepository;
	@PersistenceContext EntityManager em;
	
	@Test
	public void 프록시_객체_확인() {
		System.out.println(memberRepository.getClass());
	}
	
	@Test
	public void testMember() {
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);
		Member findMember = memberRepository.findById(savedMember.getId()).get();
		
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}
	
	@Test
	public void 기본_CRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);
		
		//단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(member1).isEqualTo(findMember1);
		assertThat(member2).isEqualTo(findMember2);
		
		//리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);
		
		//카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);
		
		//삭제 검증
		memberRepository.delete(findMember1);
		memberRepository.delete(findMember2);
		count = memberRepository.count();
		assertThat(count).isEqualTo(0);
	}
	
	@Test
	public void JPQL_메서드_테스트() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
		
		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}
	
	@Test
	public void Query_테스트() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<Member> result = memberRepository.findMember("AAA", 10);
		assertThat(result.get(0)).isEqualTo(m1);
	}
	
	public void findUsernameList() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<String> usernameList = memberRepository.findUsernameList();
	}
	
	@Test
	public void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);
		Member m1 = new Member("AAA", 10, team);
		memberRepository.save(m1);
		
		List<MemberDto> memberDto = memberRepository.findMemberDto();
		memberDto.forEach(System.out::println);
	}
	
	@Test
	public void findByNames() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		result.forEach(System.out::println);
	}
	
	@Test
	public void 반환_타입_테스트() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("AAA", 20, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<Member> mList = memberRepository.findListByUsername("AAA");
		//NonUniqueException --> IncorrectResultSizeDataAccessException 으로 바꿔버림.
		//Repository의 기술은 JPA, Mongo 등 다른 기술이 될 수 있는데, 그걸 사용하는 서비스 계층(클라이언트 코드)은 Spring에 의존하도록 설계한 것!!
		//결과가 NULL일 수 있음. JPA에서는 NoResultException인데, Spring data jpa에서 Null로 반환하기로 함. 유니크하지 않으면 Exception 발생
		assertThrows(IncorrectResultSizeDataAccessException.class, ()->{
			Member m = memberRepository.findMemberByUsername("AAA");
			});
		
//		Optional<Member> mOpt = memberRepository.findOptionalByUsername("AAA");
//		System.out.println(mOpt);
	}
	
	@Test
	public void paging() {
		memberRepository.save(new Member("member1", 10, null));
		memberRepository.save(new Member("member2", 10, null));
		memberRepository.save(new Member("member3", 10, null));
		memberRepository.save(new Member("member4", 10, null));
		memberRepository.save(new Member("member5", 10, null));

		int age = 10;
		//Pageable 인터페이스 구현체
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
		
		//when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);
		
		//DTO로 변환
//		Page<MemberDto> memberDtos = page.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName()));
		
		//then
		List<Member> content = page.getContent();
		long totalElements = page.getTotalElements();
		
		content.forEach(System.out::println);
		System.out.println(totalElements);
		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getTotalElements()).isEqualTo(5);
		assertThat(page.getNumber()).isEqualTo(0);	//페이지 번호
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}
	
	@Test
	public void slicing() {
		memberRepository.save(new Member("member1", 10, null));
		memberRepository.save(new Member("member2", 10, null));
		memberRepository.save(new Member("member3", 10, null));
		memberRepository.save(new Member("member4", 10, null));
		memberRepository.save(new Member("member5", 10, null));

		int age = 10;
		//Pageable 인터페이스 구현체
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
		
		//when
		Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);
		
		//then
		List<Member> content = page.getContent();
		
		content.forEach(System.out::println);
		assertThat(content.size()).isEqualTo(3);
		assertThat(page.getNumber()).isEqualTo(0);	//페이지 번호
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}
	
	@Test
	public void bulkUpdate() {
		Member m1 = new Member("AAA", 10, null);
		Member m2 = new Member("BBB", 20, null);
		Member m3 = new Member("CCC", 30, null);
		
		memberRepository.save(m1);
		memberRepository.save(m2);
		memberRepository.save(m3);
		
		int successCount = memberRepository.bulkUpdateAge(20);
		
		assertThat(successCount).isEqualTo(2);
		
		Member findMember = memberRepository.findByUsername("BBB");
		assertThat(findMember.getAge()).isEqualTo(21);
		
	}
	
	@Test
	public void queryHint() {
		//given
		Member member = new Member("member1", 10, null);
		memberRepository.save(member);
		em.flush();
		em.clear();
		
		//when
		//스냅샷을 아예 안만든다.
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");
		
		em.flush();
	}
	
	@Test
	public void lock() {
		//given
		Member member = new Member("member1", 10, null);
		memberRepository.save(member);
		em.flush();
		em.clear();
		
		//when
		List<Member> findMember = memberRepository.findLockByUsername("member1");
	}
}
