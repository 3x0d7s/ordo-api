package com.kyut.ordo.user;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.BoardMemberEntity;
import com.kyut.ordo.comment.CommentEntity;
import com.kyut.ordo.task.TaskEntity;
import com.kyut.ordo.workspace.WorkspaceEntity;
import com.kyut.ordo.workspace.WorkspaceMemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = {"provider", "provider_id"})
       })
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private String provider;
    private String providerId;
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "createdBy")
    private List<WorkspaceEntity> createdWorkspaces;

    @OneToMany(mappedBy = "user")
    private List<WorkspaceMemberEntity> workspaceMemberships;

    @OneToMany(mappedBy = "user")
    private List<BoardMemberEntity> boardMemberships;

    @OneToMany(mappedBy = "createdBy")
    private List<TaskEntity> createdTasks;

    @OneToMany(mappedBy = "assignedTo")
    private List<TaskEntity> assignedTasks;

    @OneToMany(mappedBy = "createdBy")
    private List<CommentEntity> comments;
}
