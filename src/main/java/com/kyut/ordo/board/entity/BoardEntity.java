package com.kyut.ordo.board.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.task.TaskListEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "boards")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_visibility", nullable = false)
    private BoardVisibility visibility;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private WorkspaceEntity workspace;

    @OneToMany(mappedBy = "board")
    private List<BoardRoleEntity> roles;

    @OneToMany(mappedBy = "board")
    private List<BoardMemberEntity> members;

    @OneToMany(mappedBy = "board")
    private List<TaskListEntity> lists;
}
