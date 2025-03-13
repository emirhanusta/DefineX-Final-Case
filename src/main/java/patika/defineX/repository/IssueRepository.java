package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Issue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    List<Issue> findAllByProjectIdAndDeletedAtNull(UUID projectId);
    Optional<Issue> findByIdAndDeletedAtNull(UUID id);
}
