package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;
import patika.defineX.model.enums.ProjectState;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany
    private List<Team> teams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectState status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Issue> issues;
}
