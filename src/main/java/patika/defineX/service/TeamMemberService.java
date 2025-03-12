package patika.defineX.service;

import org.springframework.stereotype.Service;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.exception.custom.AlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;
import patika.defineX.repository.TeamMemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<TeamMemberResponse> getAllByTeamId(UUID teamId) {
        return teamMemberRepository.findAllByTeamIdAndIsDeletedFalse(teamId)
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    public void createTeamMember(Team team, User user) {
        Optional<TeamMember> existingTeamMember = teamMemberRepository.findByTeamAndUser(team, user);

        if (existingTeamMember.isPresent()) {
            TeamMember teamMember = existingTeamMember.get();
            if (teamMember.isDeleted()) {

                teamMember.setDeleted(false);
                teamMemberRepository.save(teamMember);
                return;
            } else {
                throw new AlreadyExistException("User is already a member of this team!");
            }
        }
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        teamMemberRepository.save(teamMember);
    }

    public void removeTeamMember(UUID id) {
        TeamMember teamMember = findById(id);
        teamMember.setDeleted(true);
        teamMemberRepository.save(teamMember);
    }

    protected TeamMember findById(UUID id) {
        return teamMemberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Team member not found with id: " + id));
    }

    public void deleteAllByTeamId(UUID teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamIdAndIsDeletedFalse(teamId);
        teamMembers.forEach(teamMember -> teamMember.setDeleted(true));
        teamMemberRepository.saveAll(teamMembers);
    }

    public void deleteAllByUserId(UUID userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserIdAndIsDeletedFalse(userId);
        teamMembers.forEach(teamMember -> teamMember.setDeleted(true));
        teamMemberRepository.saveAll(teamMembers);
    }
}
