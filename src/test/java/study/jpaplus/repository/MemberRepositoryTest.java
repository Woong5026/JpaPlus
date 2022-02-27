package study.jpaplus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.jpaplus.dto.MemberDto;
import study.jpaplus.entity.Member;
import study.jpaplus.entity.Team;

import javax.persistence.EntityManager;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    public void testMember() throws Exception{
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();
    }

    @Test
    public void basicCURD() {

        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(memberRepository);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }
    
    @Test
    public void namedQuery() throws Exception{
        // given
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        Member member3 = new Member("CCC");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        // when
        List<Member> result = memberRepository.findByUsername("BBB");
        Member findMember = result.get(0);

        assertThat(findMember).isEqualTo(member2);

        System.out.println("result size = " + result.size());
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void findUserNameList() throws Exception{
        // given
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        memberRepository.save(member1);
        memberRepository.save(member2);
        // when
        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
        // then
    }

    @Test
    public void findMemberDto() throws Exception{
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("AAA", 10);
        member.setTeam(team);
        memberRepository.save(member);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() throws Exception{
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNamse(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        };
    }

    @Test
    public void returnType() throws Exception{
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Optional<Member> findMember = memberRepository.findOptionalByUsername("fdafaaf");
        System.out.println("findMember = " + findMember);
    }

    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }
    @Test
    public void bulkUpdate() throws Exception{
        // given
        memberRepository.save(new Member("member1", 11));
        memberRepository.save(new Member("member2", 13));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 30));
        memberRepository.save(new Member("member5", 40));
        // when
        int resultCount = memberRepository.bulkAgePlus(20);
        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void queryHint() throws Exception{
        // given
        Member member = new Member("hihi", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();
        // when
        Member findMember = memberRepository.findReadOnlyByUsername("hihi");
        findMember.setUsername("byebye");

        em.flush();
        // then
    }

    @Test
    public void callCustom() throws Exception{
        List<Member> result = memberRepository.findMemberCustom();

    }
    @Test
    public void JpaEventBaseEntity() throws Exception{
        // given
        Member member = new Member("member1");
        memberRepository.save(member); //@PrePersist

        Thread.sleep(100);
        member.setUsername("member2");

        em.flush(); //@PreUpdate
        em.clear();

        // when
        Member findMember = memberRepository.findById(member.getId()).get();

        // then
        System.out.println("findMember.cre = " + findMember.getCreateDate());
        System.out.println("findMember.modi = " + findMember.getLastModifedDate());
        System.out.println("findMember.creperson = " + findMember.getCreatedBy());
        System.out.println("findMember.modiperson = " + findMember.getLastModifiedBy());
    }
}