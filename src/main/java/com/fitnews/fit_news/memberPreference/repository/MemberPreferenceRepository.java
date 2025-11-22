package com.fitnews.fit_news.memberPreference.repository;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberPreferenceRepository extends JpaRepository<MemberPreference, Long> {
    Optional<MemberPreference> findByMember_Id(Long memberId);
    boolean existsByMember(Member member);
    Optional<MemberPreference> findByMember(Member member);
}

