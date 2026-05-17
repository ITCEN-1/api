package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer dongCode;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable=false) private Long viewCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Board(User user, Integer dongCode, String title, String content) {
        this.user = user;
        this.dongCode = dongCode;
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
    }
    @PrePersist
    public void increaseViewCount() { this.viewCount++; }
}