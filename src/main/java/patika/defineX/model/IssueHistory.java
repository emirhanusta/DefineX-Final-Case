package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;
import patika.defineX.model.enums.IssueStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
