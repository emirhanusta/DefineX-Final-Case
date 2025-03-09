package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;
import patika.defineX.model.enums.IssueStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String userStory;

    @Column(columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityLevel priority;

    private LocalDateTime dueDate;

    @OneToMany
    private List<IssueComment> comments;

    @OneToMany
    private List<IssueHistory> histories;

    @OneToMany
    private List<IssueAttachment> attachments;
}
