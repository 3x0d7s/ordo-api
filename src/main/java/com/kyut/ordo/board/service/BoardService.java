package com.kyut.ordo.board.service;

import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.repository.WorkspaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.dto.*;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardMemberEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.entity.BoardVisibility;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.mapper.BoardMapper;
import com.kyut.ordo.board.mapper.BoardMemberMapper;
import com.kyut.ordo.board.mapper.BoardRoleMapper;
import com.kyut.ordo.board.repository.BoardMemberRepository;
import com.kyut.ordo.board.repository.BoardRepository;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.user.repository.UserRepository;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.workspace.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRoleRepository boardRoleRepository;
    private final BoardPermissionService boardPermissionService;
    private final BoardMapper boardMapper;
    private final BoardRoleMapper boardRoleMapper;
    private final BoardMemberMapper boardMemberMapper;
    private final BoardRoleFactory boardRoleFactory;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<BoardRead> findAllAccessibleBoards(UserEntity user, Pageable pageable) {
        Page<BoardEntity> allBoards = boardRepository.findAll(pageable);
        
        List<BoardRead> accessibleBoards = allBoards.getContent().stream()
                .filter(board -> canUserAccessBoard(user, board))
                .map(boardMapper::toDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(accessibleBoards, pageable, allBoards.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<BoardRead> findAllBoardsByWorkspace(UserEntity user, Long workspaceId, Pageable pageable) {
        Page<BoardEntity> workspaceBoards = boardRepository.findAllByWorkspaceId(workspaceId, pageable);
        
        List<BoardRead> accessibleBoards = workspaceBoards.getContent().stream()
                .filter(board -> canUserAccessBoard(user, board))
                .map(boardMapper::toDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(accessibleBoards, pageable, workspaceBoards.getTotalElements());
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
        WorkspaceEntity workspace = null;

        if (dto.getWorkspaceId() != null) {
            workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new InsufficientBoardPermissionsException(
                    "Workspace not found with id: " + dto.getWorkspaceId()));

            WorkspaceMemberEntity workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(
                    dto.getWorkspaceId(),
                            user.getId())
                .orElseThrow(() -> new InsufficientBoardPermissionsException(
                    "User does not have access to this workspace"));

            if (!workspaceMember.getRole().isAbleToManageContent()) {
                throw new InsufficientBoardPermissionsException(
                    "User does not have permission to create boards in this workspace");
            }
        }

        BoardEntity board = boardMapper.toEntity(dto, workspace);
        board = boardRepository.save(board);

        Map<String, BoardRoleEntity> roles = boardRoleFactory.rolesAsMap(board);
        boardPermissionService.addMember(board, user, roles.get("Owner"));

        return boardMapper.toDto(board, roles.values());
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

    public BoardRead deleteBoard(UserEntity user, Long id)
            throws InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository
                .findById(id)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        if (!boardPermissionService.hasPermission(id, user.getId(), "DELETE")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to delete this board");
        }

        boardRepository.delete(board);

        return boardMapper.toDto(board);
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

        UserEntity newUser = userRepository.findById(newUserId).get();

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
        if (boardMemberRepository.findByBoardIdAndUserId(board.getId(), user.getId()).isPresent()) {
            return true;
        }

        if (board.getVisibility() == BoardVisibility.PUBLIC) {
            return true;
        }

        if (board.getVisibility() == BoardVisibility.WORKSPACE && board.getWorkspace() != null) {
            return workspaceMemberRepository.findByWorkspaceIdAndUserId(
                board.getWorkspace().getId(), user.getId()).isPresent();
        }

        return false;
    }
    
    @Transactional(readOnly = true)
    public Page<BoardRoleRead> findRolesByBoardId(UserEntity user, Long boardId, Pageable pageable) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
            
        if (!canUserAccessBoard(user, board)) {
            throw new InsufficientBoardPermissionsException("User does not have access to this board");
        }
        
        return boardRoleRepository
                .findAllByBoard(board, pageable)
                .map(boardRoleMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<BoardMemberRead> findMembersByBoardId(UserEntity user, Long boardId, Pageable pageable) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
            
        if (!canUserAccessBoard(user, board)) {
            throw new InsufficientBoardPermissionsException("User does not have access to this board");
        }
        
        return boardMemberRepository
                .findAllByBoard(board, pageable)
                .map(boardMemberMapper::toDto);
    }
    
    @Transactional
    public BoardRoleRead createRole(UserEntity user, BoardRoleCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + dto.getBoardId()));
            
        if (!boardPermissionService.hasPermission(dto.getBoardId(), user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to manage roles");
        }
        
        BoardRoleEntity role = boardRoleMapper.toEntity(dto);
        role.setBoard(board);
        
        return boardRoleMapper.toDto(boardRoleRepository.save(role));
    }
    
    @Transactional
    public BoardRoleRead updateRole(UserEntity user, Long roleId, BoardRoleUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardRoleEntity role = boardRoleRepository.findById(roleId)
            .orElseThrow(() -> new BoardNotFoundException("Board role not found with id: " + roleId));
            
        if (!boardPermissionService.hasPermission(role.getBoard().getId(), user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to manage roles");
        }
        
        boardRoleMapper.updateEntityFromDto(dto, role);
        
        return boardRoleMapper.toDto(boardRoleRepository.save(role));
    }
    
    @Transactional
    public BoardRoleRead getMyRole(UserEntity user, Long boardId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
            
        if (!canUserAccessBoard(user, board)) {
            throw new InsufficientBoardPermissionsException("User does not have access to this board");
        }
        
        // First search BoardMemberEntity
        BoardMemberEntity boardMember = boardMemberRepository.findByBoardIdAndUserId(boardId, user.getId())
            .orElse(null);
            
        if (boardMember != null) {
            return boardRoleMapper.toDto(boardMember.getRole());
        }
        
        if (board.getVisibility() == BoardVisibility.PUBLIC) {
            BoardRoleEntity guestRole = boardRoleRepository
                .findByBoardAndName(board, "Guest")
                .orElseGet(() -> boardRoleFactory.createGuestRole(board));
            
            BoardMemberEntity newBoardMember = BoardMemberEntity.builder()
                .board(board)
                .user(user)
                .role(guestRole)
                .joinedAt(LocalDateTime.now())
                .build();
                
            boardMemberRepository.save(newBoardMember);
            
            return boardRoleMapper.toDto(guestRole);
        }
        
        if (board.getVisibility() != BoardVisibility.WORKSPACE || board.getWorkspace() == null) {
            throw new BoardNotFoundException("User is not a member of this board and has no access");
        }

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(board.getWorkspace().getId(), user.getId())
                .orElse(null);

        if (workspaceMember != null) {
            throw new BoardNotFoundException("User is not a member of this board and has no access");
        }

        BoardRoleEntity memberRole = boardRoleRepository
                .findByBoardAndName(board, "Member")
                .orElseGet(() -> boardRoleFactory.createMemberRole(board));

        BoardMemberEntity newBoardMember = BoardMemberEntity.builder()
                .board(board)
                .user(user)
                .role(memberRole)
                .joinedAt(LocalDateTime.now())
                .build();

        boardMemberRepository.save(newBoardMember);

        return boardRoleMapper.toDto(memberRole);

    }
    
    @Transactional
    public BoardMemberRead createMember(UserEntity user, Long boardId, BoardMemberCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
            
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "INVITE")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to invite members");
        }
        
        UserEntity newUser = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new BoardNotFoundException("User not found with id: " + dto.getUserId()));
            
        BoardRoleEntity role = boardRoleRepository.findById(dto.getBoardRoleId())
            .orElseThrow(() -> new BoardNotFoundException("Board role not found with id: " + dto.getBoardRoleId()));
            
        BoardMemberEntity member = BoardMemberEntity.builder()
                .board(board)
                .user(newUser)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
                
        return boardMemberMapper.toDto(boardMemberRepository.save(member));
    }
    
    @Transactional
    public BoardMemberRead updateMember(UserEntity user, Long boardId, Long memberId, BoardMemberUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to manage roles");
        }
        
        BoardMemberEntity member = boardMemberRepository.findById(memberId)
            .orElseThrow(() -> new BoardNotFoundException("Board member not found with id: " + memberId));
            
        if (!member.getBoard().getId().equals(boardId)) {
            throw new BoardNotFoundException("Board member does not belong to this board");
        }
        
        BoardRoleEntity role = boardRoleRepository.findById(dto.getBoardRoleId())
            .orElseThrow(() -> new BoardNotFoundException("Board role not found with id: " + dto.getBoardRoleId()));
            
        member.setRole(role);
        
        return boardMemberMapper.toDto(boardMemberRepository.save(member));
    }
    
    @Transactional
    public BoardMemberRead deleteMember(UserEntity user, Long boardId, Long memberId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "INVITE")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to manage members");
        }
        
        BoardMemberEntity member = boardMemberRepository.findById(memberId)
            .orElseThrow(() -> new BoardNotFoundException("Board member not found with id: " + memberId));
            
        if (!member.getBoard().getId().equals(boardId)) {
            throw new BoardNotFoundException("Board member does not belong to this board");
        }
        
        boardMemberRepository.delete(member);
        
        return boardMemberMapper.toDto(member);
    }
}
