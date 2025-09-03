package com.kyut.ordo.feature.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
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
    @ToString.Exclude
    private WorkspaceEntity workspace;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    private List<WorkspaceMemberEntity> members;

    @Column(nullable = false)
    private boolean ableToManageMembers;
    @Column(nullable = false)
    private boolean ableToManageContent;
    @Column(nullable = false)
    private boolean ableToManageSettings;
    @Column(nullable = false)
    private boolean ableToManageRoles;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        WorkspaceRoleEntity that = (WorkspaceRoleEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
