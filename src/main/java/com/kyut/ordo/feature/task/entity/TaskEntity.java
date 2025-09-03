package com.kyut.ordo.feature.task.entity;

import com.kyut.ordo.feature.card.entity.CardEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;
}
