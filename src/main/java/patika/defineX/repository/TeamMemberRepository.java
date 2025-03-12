package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findAllByTeamIdAndIsDeletedFalse(UUID id);

    Optional<TeamMember> findByIdAndIsDeletedFalse(UUID id);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);
}
