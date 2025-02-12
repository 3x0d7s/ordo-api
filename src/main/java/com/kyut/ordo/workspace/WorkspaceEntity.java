package com.kyut.ordo.workspace;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.BoardEntity;
import com.kyut.ordo.user.UserEntity;

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
import org.springframework.data.annotation.CreatedDate;

@Data
@Entity
@Table(name = "workspaces")
public class WorkspaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;



    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity owner;

    // Relationships
    @OneToMany(mappedBy = "workspace")
    private List<WorkspaceMemberEntity> members;

    @OneToMany(mappedBy = "workspace")
    private List<BoardEntity> boards;
}
