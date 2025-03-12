package patika.defineX.service;

import org.springframework.stereotype.Service;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;
import patika.defineX.repository.TeamMemberRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<TeamMember> addTeamMember(Team team, User user) {
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        teamMemberRepository.save(teamMember);
        return teamMemberRepository.findAllByTeamIdAndIsDeletedFalse(team.getId());
    }

    public void removeTeamMember(UUID id) {
        TeamMember teamMember = findById(id);
        teamMember.setDeleted(true);
        teamMemberRepository.save(teamMember);
    }

    private TeamMember findById(UUID id) {
        return teamMemberRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Team member not found with id: " + id));
    }
}
