package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.IssueAttachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, UUID> {
    List<IssueAttachment> findByIssueIdAndDeletedAtNull(UUID issueId);
    Optional<IssueAttachment> findByIdAndDeletedAtNull(UUID id);
}
