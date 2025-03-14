package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByDeletedAtNull();
    Optional<User> findByIdAndDeletedAtNull(UUID id);
    boolean existsByEmailAndDeletedAtNull(String email);
    Optional<User> findByEmailAndDeletedAtNull(String username);
}
