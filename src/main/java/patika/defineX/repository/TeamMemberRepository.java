package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findAllByTeamIdAndDeletedAtNull(UUID id);
    Optional<TeamMember> findByIdAndDeletedAtNull(UUID id);
    Optional<TeamMember> findByTeamAndUserAndDeletedAtNull(Team team, User user);
    List<TeamMember> findAllByUserIdAndDeletedAtNull(UUID userId);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN tm.team t " +
            "JOIN t.project p " +
            "WHERE p.id = :projectId " +
            "AND p.deletedAt IS NULL " +
            "AND t.deletedAt IS NULL " +
            "AND tm.deletedAt IS NULL")
    List<TeamMember> findAllByProjectIdAndDeletedAtNull(UUID projectId);
}
