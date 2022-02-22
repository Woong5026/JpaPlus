package study.jpaplus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.jpaplus.entity.Member;

public interface MemberRepository extends JpaRepository<Member,Long> {

}
