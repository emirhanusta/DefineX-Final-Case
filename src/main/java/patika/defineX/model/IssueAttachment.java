package patika.defineX.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAttachment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Issue issue;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;
}
