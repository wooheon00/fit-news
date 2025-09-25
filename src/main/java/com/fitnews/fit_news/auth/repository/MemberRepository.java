package com.fitnews.fit_news.auth.repository;

import com.fitnews.fit_news.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByRefreshToken(String refreshToken);
    Optional<Member> findByUsername(String username);
}
