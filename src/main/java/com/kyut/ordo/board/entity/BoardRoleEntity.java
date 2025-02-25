package com.kyut.ordo.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_roles")
public class BoardRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private boolean ableToEdit;
    private boolean ableToDelete;
    private boolean ableToInviteMembers;
    private boolean ableToManageRoles;
    private boolean ableToCreateLists;
    private boolean ableToCreateTasks;
}
