package me.mocha.backend.common.model.repository;

import me.mocha.backend.common.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsernameOrNicknameOrEmail(String username, String nickname, String email);
}
