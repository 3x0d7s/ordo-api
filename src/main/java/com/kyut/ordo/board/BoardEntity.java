package com.kyut.ordo.board;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.BoardMemberEntity;
import com.kyut.ordo.task.TaskListEntity;
import com.kyut.ordo.workspace.WorkspaceEntity;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    // Relationships
    @OneToMany(mappedBy = "board")
    private List<BoardMemberEntity> members;

    @OneToMany(mappedBy = "board")
    private List<TaskListEntity> lists;
}
