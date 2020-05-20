package com.study.datajpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.study.datajpa.dto.MemberDto;
import com.study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{
	/**
	 * 메서드 이름으로 쿼리 생성
	 * 1. find...ByXxxAndXxx 형태로 쿼리를 생성할 수 있다.
	 * 장점 : 간단한 쿼리를 직접 생성하지 않을 수 있다.
	 * 단점 : 쿼리가 복잡해지면 메서드 이름이 너무 길어진다.
	 */
	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
	Member findByUsername(String username);
	
	/**
	 * Named Query
	 * 장점 : 로딩 시점에 오류를 잡아주는 장점이 있다.
	 * 단점 : 엔티티에 NamedQuery를 정의해야 한다는 것이 매우 번거롭다.
	 */
//	@Query("Member.findByUsername")
//	List<Member> findNamedQueryByUsername(@Param("name") String name);
	

	/**
	 * @Query를 사용해 Entity 조회
	 * 장점 : NamedQuery를 엔티티가 아닌 Repository에 작성하는 것으로, 가독성이 좋다. 사용이 편리하다. 로딩 시점에 오류를 잡아준다.
	 * 단점 : 쿼리를 직접 작성해야 한다. 간단한 쿼리인 경우에는 메서드 이름으로 쿼리를 생성하자.
	 */
	@Query("select m from Member m where m.username = :username and m.age = :age")
	List<Member> findMember(@Param("username") String username, @Param("age") int age);

	
	/**
	 * Dto로 조회
	 * 1. Value Object(임베디드 타입)으로 조회가 가능하다.
	 * 2. new 연산을 사용해서 바로 조회가 가능하다. -> QueryDSL을 사용하면 코드가 더욱 간결
	 * 3. 엔티티로 조회한 후에 Dto로 변환하는 방법도 자주 사용된다.
	 */
	@Query("select m.username from Member m")
	List<String> findUsernameList();
	@Query("select new com.study.datajpa.dto.MemberDto(m.id, m.username,t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();
	
	/**
	 * In쿼리
	 * 1. Collection을 인자로 받아서 in절의 아규먼트로 사용이 가능하다. in절은 최적화를 할 때 매우 좋은 기능이다.
	 */
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") Collection<String> names);
	
	
	/**
	 * 리턴 타입
	 * 1. Collection<T> : 컬렉션의 형태로 데이터 조회가 가능하다. 데이터가 없을 경우 빈 컬렉션이 반환된다.
	 * 2. T : 단건 조회에 사용된다. 데이터가 없을 경우 null을 반환하고 데이터가 2개이상인 경우 NonUniqueException (JPA)  -->  IncorrectResultSizeDataAccessException (Spring)
	 * 3. Optional<T> : 자바8이상 권장사항이며 단건 조회에 사용된다. 데이터가 없을 수도 있음을 나타낸다. orElse() 같은 연산을 사용해서 처리한다.
	 */
	List<Member> findListByUsername(String username);	//컬렉션
	Member findMemberByUsername(String username); 		//단건
	Optional<Member> findOptionalByUsername(String username); //단건 Optional
	
	/**
	 * 페이징 처리
	 * 1. Page : 데이터 전체 개수 조회 가능. 데이터 개수를 세는 쿼리는 별도로 작성 가능.
	 * 2. Slice : 데이터 전체 개수 조회 안함. limit+1을 조회하여 다음 페이지가 있는지 여부만 알려줌.
	 */
	@Query(value = "select m from Member m left join m.team t",
			countQuery = "select count(m) from Member m")
	Page<Member> findByAge(int age, Pageable pageable);
	Slice<Member> findSliceByAge(int age, Pageable pageable);

	/**
	 * 벌크성 연산
	 * 1. @Query를 이용해서 update를 직접 작성한다.
	 * 2. @Modifying과 함께 사용하지 않으면 예외가 발생한다.
	 * 3. clearAutomatically = true로 설정하지 않거나 영속성 컨텍스트를 비우지 않으면, 영속성 컨텍스트와 DB의 불일치가 발생할 수 있다.
	 * (벌크 연산은 영속성 컨텍스트를 통하지 않기 때문) 
	 */
	@Modifying(clearAutomatically = true)
	@Query(value = "update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkUpdateAge(@Param("age") int age);
	
	/**
	 * 페치조인
	 * 1. @Query를 이용해서 fetch join을 직접 사용한다.
	 * 2. @EntityGraph를 이용해서 페치 조인할 엔티티를 넘겨준다.
	 * 3. @EntityGraph는 @Query와 함께 쓰일 수도 있고, 메서드 @Override와도 함께 쓰일 수 있다.
	 */
	@Query("select m from Member m join fetch m.team")
	List<Member> findMemberFetchJoin();
	@EntityGraph(attributePaths={"team"})
	List<Member> findEntityGraphByUsername(String username);

	@QueryHints(value = @QueryHint(name = "org.hiberante.readOnly", value = "true"))
	Member findReadOnlyByUsername(String username);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Member> findLockByUsername(String username);
}
