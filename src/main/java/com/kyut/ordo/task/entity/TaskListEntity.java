package com.kyut.ordo.task.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.entity.BoardEntity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lists")
@Data
public class TaskListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer position;

    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    // Relationships
    @OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskEntity> tasks;
}
