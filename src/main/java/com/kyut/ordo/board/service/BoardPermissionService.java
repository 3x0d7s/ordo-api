package com.kyut.ordo.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardMemberEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.repository.BoardMemberRepository;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardPermissionService {
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRoleRepository boardRoleRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Long boardId, Long userId, String permission) {
        Optional<BoardMemberEntity> memberOpt = boardMemberRepository.findByBoardIdAndUserId(boardId, userId);
        if (memberOpt.isEmpty()) {
            return false;
        }

        BoardRoleEntity role = memberOpt.get().getRole();
        return switch (permission) {
            case "EDIT" -> role.isAbleToEdit();
            case "DELETE" -> role.isAbleToDelete();
            case "INVITE" -> role.isAbleToInviteMembers();
            case "MANAGE_ROLES" -> role.isAbleToManageRoles();
            case "CREATE_LISTS" -> role.isAbleToCreateLists();
            case "CREATE_TASKS" -> role.isAbleToCreateTasks();
            default -> false;
        };
    }

    @Transactional
    public BoardMemberEntity addMember(BoardEntity board, UserEntity user, BoardRoleEntity role) {
        BoardMemberEntity member = BoardMemberEntity.builder()
            .board(board)
            .user(user)
            .role(role)
            .joinedAt(LocalDateTime.now())
            .build();

        return boardMemberRepository.save(member);
    }

    @Transactional
    public BoardMemberEntity updateMemberRole(Long boardId, Long userId, Long newRoleId) {
        BoardMemberEntity member = boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        BoardRoleEntity newRole = boardRoleRepository.findById(newRoleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        member.setRole(newRole);
        return boardMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<BoardMemberEntity> getBoardMembers(Long boardId) {
        return boardMemberRepository.findAllByBoardId(boardId);
    }
}
