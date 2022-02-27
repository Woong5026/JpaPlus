package study.jpaplus.repository;

import study.jpaplus.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
