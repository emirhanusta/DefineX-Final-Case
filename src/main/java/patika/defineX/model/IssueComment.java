package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueComment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Issue issue;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;
}
