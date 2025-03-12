package patika.defineX.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import patika.defineX.event.TeamDeletedEvent;
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

    public TeamMember createTeamMember(Team team, User user) {
        Optional<TeamMember> existingTeamMember = teamMemberRepository.findByTeamAndUser(team, user);

        if (existingTeamMember.isPresent()) {
            TeamMember teamMember = existingTeamMember.get();
            if (teamMember.isDeleted()) {

                teamMember.setDeleted(false);
                return teamMemberRepository.save(teamMember);
            } else {
                throw new AlreadyExistException("User is already a member of this team!");
            }
        }
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        return teamMemberRepository.save(teamMember);
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

    @EventListener
    public void deleteAllByTeamId(TeamDeletedEvent event) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamIdAndIsDeletedFalse(event.teamId());
        teamMembers.forEach(teamMember -> teamMember.setDeleted(true));
        teamMemberRepository.saveAll(teamMembers);
    }
}
