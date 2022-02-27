package study.jpaplus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.jpaplus.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

}
