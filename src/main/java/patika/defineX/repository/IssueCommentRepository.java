package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.IssueComment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueCommentRepository extends JpaRepository<IssueComment, UUID> {
    List<IssueComment> findAllByIssueIdAndIsDeletedFalse(UUID issueId);

    Optional<IssueComment> findByIdAndIsDeletedFalse(UUID id);
}
