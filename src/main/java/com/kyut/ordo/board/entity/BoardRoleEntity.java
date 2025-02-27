package com.kyut.ordo.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "board_roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @OneToMany(mappedBy = "role")
    private List<BoardMemberEntity> members;

    @Column(nullable = false)
    private boolean ableToEdit;
    @Column(nullable = false)
    private boolean ableToDelete;
    @Column(nullable = false)
    private boolean ableToInviteMembers;
    @Column(nullable = false)
    private boolean ableToManageRoles;
    @Column(nullable = false)
    private boolean ableToCreateLists;
    @Column(nullable = false)
    private boolean ableToCreateTasks;
}
