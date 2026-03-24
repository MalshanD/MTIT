package com.onlinelearning.auth.repository;

import com.onlinelearning.auth.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    Optional<AccessToken> findByTokenValue(String tokenValue);

    void deleteByUserId(Long userId);

    void deleteByTokenValue(String tokenValue);
}
