package com.kyut.ordo.board.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.entity.BoardVisibility;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.mapper.BoardMapper;
import com.kyut.ordo.board.repository.BoardMemberRepository;
import com.kyut.ordo.board.repository.BoardRepository;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.workspace.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRoleRepository boardRoleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final BoardMapper boardMapper;
    private final BoardRoleFactory boardRoleFactory;

    @Transactional(readOnly = true)
    public Page<BoardRead> findAllAccessibleBoards(UserEntity user, Pageable pageable) {
        return boardRepository
                .findAll(pageable)
                .map(board -> {
                    if (canUserAccessBoard(user, board)) {
                        return boardMapper.toDto(board);
                    }
                return null;
            }).map(dto -> dto);
    }

    @Transactional(readOnly = true)
    public BoardRead findById(UserEntity user, Long id)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        if (!canUserAccessBoard(user, board)) {
            throw new InsufficientBoardPermissionsException("User does not have access to this board");
        }

        return boardMapper.toDto(board);
    }

    @Transactional
    public BoardRead createBoard(UserEntity user, BoardCreate dto)
            throws InsufficientBoardPermissionsException {
        if (dto.getWorkspaceId() != null) {
            WorkspaceMemberEntity workspace = workspaceMemberRepository.findByWorkspaceIdAndUserId(
                    dto.getWorkspaceId(),
                            user.getId())
                .orElseThrow(() -> new InsufficientBoardPermissionsException(
                    "User does not have access to this workspace"));

            if (!workspace.getRole().isAbleToManageContent()) {
                throw new InsufficientBoardPermissionsException(
                    "User does not have permission to create boards in this workspace");
            }
        }

        BoardEntity board = boardMapper.toEntity(dto);
        board = boardRepository.save(board);

        // Create roles using factory
        BoardRoleEntity ownerRole = boardRoleFactory.createOwnerRole();
        boardRoleFactory.createMemberRole();
        boardRoleFactory.createGuestRole();

        boardPermissionService.addMember(board, user, ownerRole);

        return boardMapper.toDto(board);
    }

    @Transactional
    public BoardRead updateBoard(UserEntity user,
                                 Long id,
                                 BoardCreate dto) throws InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        if (!boardPermissionService.hasPermission(id, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to edit this board");
        }

        boardMapper.updateEntityFromDto(dto, board);
        board = boardRepository.save(board);

        return boardMapper.toDto(board);
    }

    @Transactional
    public void deleteBoard(UserEntity user, Long id)
            throws InsufficientBoardPermissionsException {
        if (!boardPermissionService.hasPermission(id, user.getId(), "DELETE")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to delete this board");
        }

        boardRepository.deleteById(id);
    }

    @Transactional
    public void addMember(UserEntity user,
                          Long boardId,
                          Long newUserId,
                          Long roleId)
            throws InsufficientBoardPermissionsException {
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "INVITE")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to add members");
        }

        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        UserEntity newUser = new UserEntity(); // TODO: Get user from UserService
        newUser.setId(newUserId);

        BoardRoleEntity role = boardRoleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        boardPermissionService.addMember(board, newUser, role);
    }

    @Transactional
    public void updateMemberRole(UserEntity user,
                                 Long boardId,
                                 Long memberId,
                                 Long newRoleId) throws InsufficientBoardPermissionsException {
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to manage roles");
        }

        boardPermissionService.updateMemberRole(boardId, memberId, newRoleId);
    }

    private boolean canUserAccessBoard(UserEntity user, BoardEntity board) {
        if (board.getVisibility() == BoardVisibility.PUBLIC) {
            return true;
        }

        if (boardMemberRepository.findByBoardIdAndUserId(board.getId(), user.getId()).isPresent()) {
            return true;
        }

        if (board.getVisibility() == BoardVisibility.WORKSPACE && board.getWorkspace() != null) {
            return workspaceMemberRepository.findByWorkspaceIdAndUserId(
                board.getWorkspace().getId(), user.getId()).isPresent();
        }

        return false;
    }
}
