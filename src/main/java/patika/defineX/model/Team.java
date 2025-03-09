package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany
    private List<TeamMember> teamMembers;
}
