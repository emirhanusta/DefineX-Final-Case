package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.BaseEntity;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;
import patika.defineX.repository.TeamMemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamMemberService {

    private static final Logger logger = LoggerFactory.getLogger(TeamMemberService.class);

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<TeamMemberResponse> getAllByTeamId(UUID teamId) {
        logger.info("Fetching team members for team with id: {}", teamId);
        List<TeamMemberResponse> teamMembers = teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId)
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
        logger.info("Found {} team members for team with id: {}", teamMembers.size(), teamId);
        return teamMembers;
    }

    public List<TeamMemberResponse> getAllMembersByProjectId(UUID projectId) {
        logger.info("Fetching team members for project with id: {} from database", projectId);
        List<TeamMemberResponse> teamMembers = teamMemberRepository.findAllByProjectIdAndDeletedAtNull(projectId)
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
        logger.info("{} team members found for project id: {}", teamMembers.size(), projectId);
        return teamMembers;
    }

    public void createTeamMember(Team team, User user) {
        logger.info("Creating team member for team with id: {} and user with id: {}", team.getId(), user.getId());
        Optional<TeamMember> existingTeamMember = teamMemberRepository.findByTeamAndUser(team, user);

        if (existingTeamMember.isPresent()) {
            TeamMember teamMember = existingTeamMember.get();
            if (teamMember.isDeleted()) {
                logger.info("Restoring deleted team member with id: {}", teamMember.getId());
                teamMember.restore();
                teamMemberRepository.save(teamMember);
                return;
            } else {
                logger.warn("User with id: {} is already a member of team with id: {}", user.getId(), team.getId());
                throw new CustomAlreadyExistException("User is already a member of this team!");
            }
        }

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);
        logger.info("Team member created with id: {}", savedTeamMember.getId());
    }

    public void removeTeamMember(UUID id) {
        logger.info("Removing team member with id: {}", id);
        TeamMember teamMember = findById(id);
        teamMember.softDelete();
        teamMemberRepository.save(teamMember);
        logger.info("Team member removed with id: {}", id);
    }

    protected TeamMember findById(UUID id) {
        logger.debug("Finding team member by id: {}", id);
        return teamMemberRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    logger.error("Team member not found with id: {}", id);
                    return new CustomNotFoundException("Team member not found with id: " + id);
                });
    }

    public void deleteAllByTeamId(UUID teamId) {
        logger.info("Deleting all team members for team with id: {}", teamId);
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId);
        teamMembers.forEach(BaseEntity::softDelete);
        teamMemberRepository.saveAll(teamMembers);
        logger.info("Deleted {} team members for team with id: {}", teamMembers.size(), teamId);
    }

    public void deleteAllByUserId(UUID userId) {
        logger.info("Deleting all team members for user with id: {}", userId);
        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserIdAndDeletedAtNull(userId);
        teamMembers.forEach(BaseEntity::softDelete);
        teamMemberRepository.saveAll(teamMembers);
        logger.info("Deleted {} team members for user with id: {}", teamMembers.size(), userId);
    }
}