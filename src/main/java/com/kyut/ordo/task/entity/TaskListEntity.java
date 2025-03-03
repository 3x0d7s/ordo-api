package com.kyut.ordo.task.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.entity.BoardEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    // Relationships
    @OneToMany(mappedBy = "list")
    private List<TaskEntity> tasks;
}
