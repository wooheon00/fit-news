package com.fitnews.fit_news.memberPreference.entity;

import com.fitnews.fit_news.auth.entity.Member;
import com.fitnews.fit_news.news.entity.NewsTendency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MemberPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    // 0 ~ 100 (보수 ~ 진보)
    private int politic;

    // 0 ~ 100 (여성향 ~ 남성향) 라고 가정
    private int gender;

    // 'a' = 미성년, 'b' = 청년, 'c' = 중년, 'd' = 노년
    private char age;

    private int clickCount;

    protected MemberPreference() {}

    public static MemberPreference createDefault(Member member) {
        MemberPreference pref = new MemberPreference();
        pref.member = member;
        pref.politic = 50;   // 중립
        pref.gender = 50;    // 중립
        pref.age = 'b';      // 대충 청년
        pref.clickCount = 0;
        return pref;
    }

    /**
     * 🔥 클릭된 뉴스 성향을 벡터 평균으로 반영
     */
    public void applyClick(NewsTendency tendency) {
        int n = this.clickCount;

        // 벡터: [politic, gender]
        this.politic = (this.politic * n + tendency.getPolitic()) / (n + 1);
        this.gender  = (this.gender  * n + tendency.getGender())  / (n + 1);

        // age는 간단히 최근 클릭 기준으로
        this.age     = tendency.getAge();

        this.clickCount = n + 1;
    }

    // getter들 생략 (Lombok 써도 됨)
}