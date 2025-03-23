package patika.defineX.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import patika.defineX.model.*;
import patika.defineX.model.enums.*;
import patika.defineX.repository.*;

import java.util.Set;

@Component
public class DataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        User admin = User.builder().name("admin").email("admin@mail.com")
                .password(passwordEncoder.encode("passss"))
                .authorities(Set.of(Role.TEAM_MEMBER, Role.PROJECT_MANAGER)).build();
        userRepository.save(admin);

    }
}