package me.mocha.backend.common.model.repository;

import me.mocha.backend.common.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRespository extends JpaRepository<User, String> {
}
