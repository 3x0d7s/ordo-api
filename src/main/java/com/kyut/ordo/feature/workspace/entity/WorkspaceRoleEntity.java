package com.kyut.ordo.feature.workspace.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "workspace_roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @OneToMany(mappedBy = "role")
    private List<WorkspaceMemberEntity> members;

    @Column(nullable = false)
    private boolean ableToManageMembers;
    @Column(nullable = false)
    private boolean ableToManageContent;
    @Column(nullable = false)
    private boolean ableToManageSettings;
    @Column(nullable = false)
    private boolean ableToManageRoles;
}
