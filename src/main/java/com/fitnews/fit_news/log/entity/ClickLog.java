package com.fitnews.fit_news.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // JWT에서 추출한 사용자 ID

    private Long newsId;  // ✅ Long

    private LocalDateTime clickedAt;

    @PrePersist
    public void prePersist() {
        this.clickedAt = LocalDateTime.now();
    }
}
