package com.kyut.ordo.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.kyut.ordo.auth.oauth2.AuthProvider;
import com.kyut.ordo.board.BoardMemberEntity;
import com.kyut.ordo.comment.CommentEntity;
import com.kyut.ordo.task.TaskEntity;
import com.kyut.ordo.workspace.WorkspaceEntity;
import com.kyut.ordo.workspace.WorkspaceMemberEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@Builder
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = {"provider", "provider_id"})
       })
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

}
