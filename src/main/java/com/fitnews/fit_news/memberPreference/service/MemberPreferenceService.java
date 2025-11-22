package com.fitnews.fit_news.memberPreference.service;

import com.fitnews.fit_news.memberPreference.entity.MemberPreference;
import com.fitnews.fit_news.memberPreference.repository.MemberPreferenceRepository;
import com.fitnews.fit_news.news.entity.NewsTendency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitnews.fit_news.auth.entity.Member;



@Service
@RequiredArgsConstructor
public class MemberPreferenceService {

    private final MemberPreferenceRepository memberPreferenceRepository;

    @Transactional
    public void createDefaultFor(Member member) {
        // 혹시 이미 있으면 또 만들지 않기
        boolean exists = memberPreferenceRepository
                .findByMember_Id(member.getId())
                .isPresent();
        if (exists) return;

        MemberPreference pref = MemberPreference.createDefault(member);
        memberPreferenceRepository.save(pref);
    }

    public boolean existsFor(Member member) {
        return memberPreferenceRepository.existsByMember(member);
    }

    @Transactional
    public void updatePreference(Long memberId, NewsTendency tendency) {

        MemberPreference pref = memberPreferenceRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new IllegalStateException("MemberPreference 없음: " + memberId));

        // 디버깅용 로그
        System.out.println("[PrefBefore] politic=" + pref.getPolitic()
                + ", gender=" + pref.getGender()
                + ", clickCount=" + pref.getClickCount());

        pref.applyClick(tendency); // politic, gender, age, clickCount 변경

        // 🔥 안전하게 명시적으로 save 한 번 해주자
        memberPreferenceRepository.save(pref);

        System.out.println("[PrefAfter] politic=" + pref.getPolitic()
                + ", gender=" + pref.getGender()
                + ", clickCount=" + pref.getClickCount());
    }

    public MemberPreference getByMemberOrCreate(Member member) {
        return memberPreferenceRepository.findByMember_Id(member.getId())
                .orElseGet(() -> {
                    MemberPreference pref = MemberPreference.createDefault(member);
                    return memberPreferenceRepository.save(pref);
                });
    }

    @Transactional
    public MemberPreference updateForOnboarding(Member member,
                                                int politic,
                                                int gender,
                                                char age) {
        MemberPreference pref = memberPreferenceRepository.findByMember(member)
                .orElseGet(() -> MemberPreference.createDefault(member));

        pref.setPolitic(politic);
        pref.setGender(gender);
        pref.setAge(age);
        // clickCount는 그대로 두고 싶으면 그대로

        return memberPreferenceRepository.save(pref); // ✅ 최종 저장
    }

}
