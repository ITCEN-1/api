package com.itset.itcenteamproject.domain.history;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "history_items")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HistoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="history_id", nullable = false)
    private History history;

    @Column(nullable = false)
    private Integer ranking;

    @Column(nullable = false)
    private Integer dongCode;

    @Builder
    public HistoryItem(History history, Integer ranking,Integer dongCode) {
        this.history = history;
        this.ranking = ranking;
        this.dongCode = dongCode;
    }
}
