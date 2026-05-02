package com.kyut.ordo.feature.board.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.kyut.ordo.feature.board.dto.BoardCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberRead;
import com.kyut.ordo.feature.board.dto.BoardMemberUpdate;
import com.kyut.ordo.feature.board.dto.BoardRead;
import com.kyut.ordo.feature.board.dto.BoardRoleCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleRead;
import com.kyut.ordo.feature.board.dto.BoardRoleUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.mapper.BoardMapper;
import com.kyut.ordo.feature.board.mapper.BoardMemberMapper;
import com.kyut.ordo.feature.board.mapper.BoardRoleMapper;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;

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
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public BoardRead findById(UserEntity user, Long id)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        return boardMapper.toDto(board);
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canCreateBoard(#p1.workspaceId, authentication)")
    public BoardRead createBoard(UserEntity user, BoardCreate dto)
            throws InsufficientBoardPermissionsException {
        WorkspaceEntity workspace = null;

        if (dto.getWorkspaceId() != null) {
            workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new InsufficientBoardPermissionsException(
                    "Workspace not found with id: " + dto.getWorkspaceId()));
        }

        BoardEntity board = boardMapper.toEntity(dto, workspace);
        board = boardRepository.save(board);

        Map<String, BoardRoleEntity> roles = boardRoleFactory.rolesAsMap(board);
        boardPermissionService.addMember(board, user, roles.get("Owner"));

        return boardMapper.toDto(board, roles.values());
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canEditBoard(#p1, authentication)")
    public BoardRead updateBoard(UserEntity user,
                                 Long id,
                                 BoardCreate dto) throws InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        boardMapper.updateEntityFromDto(dto, board);
        board = boardRepository.save(board);

        return boardMapper.toDto(board);
    }

    @PreAuthorize("@featureAuthService.canDeleteBoard(#p1, authentication)")
    public BoardRead deleteBoard(UserEntity user, Long id)
            throws InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository
                .findById(id)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + id));

        boardRepository.delete(board);

        return boardMapper.toDto(board);
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canInviteBoardMembers(#p1, authentication)")
    public void addMember(UserEntity user,
                          Long boardId,
                          Long newUserId,
                          Long roleId)
            throws InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        UserEntity newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BoardRoleEntity role = boardRoleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        boardPermissionService.addMember(board, newUser, role);
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canManageBoardRoles(#p1, authentication)")
    public void updateMemberRole(UserEntity user,
                                 Long boardId,
                                 Long memberId,
                                 Long newRoleId) throws InsufficientBoardPermissionsException {
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
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public Page<BoardRoleRead> findRolesByBoardId(UserEntity user, Long boardId, Pageable pageable) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        return boardRoleRepository
                .findAllByBoard(board, pageable)
                .map(boardRoleMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public Page<BoardMemberRead> findMembersByBoardId(UserEntity user, Long boardId, Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        return boardMemberRepository
                .findAllByBoard(board, pageable)
                .map(boardMemberMapper::toDto);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canManageBoardRoles(#p1.boardId, authentication)")
    public BoardRoleRead createRole(UserEntity user, BoardRoleCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + dto.getBoardId()));

        BoardRoleEntity role = boardRoleMapper.toEntity(dto);
        role.setBoard(board);
        
        return boardRoleMapper.toDto(boardRoleRepository.save(role));
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canManageBoardRolesByRoleId(#p1, authentication)")
    public BoardRoleRead updateRole(UserEntity user, Long roleId, BoardRoleUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardRoleEntity role = boardRoleRepository.findById(roleId)
            .orElseThrow(() -> new BoardNotFoundException("Board role not found with id: " + roleId));

        boardRoleMapper.updateEntityFromDto(dto, role);
        
        return boardRoleMapper.toDto(boardRoleRepository.save(role));
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public BoardRoleRead getMyRole(UserEntity user, Long boardId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
        // Return existing role if user is already a member
        BoardMemberEntity existingMember = boardMemberRepository
                .findByBoardIdAndUserId(boardId, user.getId())
                .orElse(null);
                
        if (existingMember != null) {
            return boardRoleMapper.toDto(existingMember.getRole());
        }
        
        // Auto-join user based on board visibility
        BoardRoleEntity role = determineAndCreateRoleForNewMember(user, board);
        createBoardMembership(board, user, role);
        
        return boardRoleMapper.toDto(role);
    }
    
    /**
     * Determines appropriate role for new member based on board visibility
     */
    private BoardRoleEntity determineAndCreateRoleForNewMember(UserEntity user, BoardEntity board) 
            throws BoardNotFoundException {
        if (board.getVisibility() == BoardVisibility.PUBLIC) {
            return getOrCreateGuestRole(board);
        }
        
        if (board.getVisibility() == BoardVisibility.WORKSPACE && board.getWorkspace() != null) {
            verifyWorkspaceMembership(user, board);
            return getOrCreateMemberRole(board);
        }
        
        throw new BoardNotFoundException("User is not a member of this board and has no access");
    }
    
    /**
     * Gets existing Guest role or creates new one
     */
    private BoardRoleEntity getOrCreateGuestRole(BoardEntity board) {
        return boardRoleRepository
                .findByBoardAndName(board, "Guest")
                .orElseGet(() -> boardRoleFactory.createGuestRole(board));
    }
    
    /**
     * Gets existing Member role or creates new one
     */
    private BoardRoleEntity getOrCreateMemberRole(BoardEntity board) {
        return boardRoleRepository
                .findByBoardAndName(board, "Member")
                .orElseGet(() -> boardRoleFactory.createMemberRole(board));
    }
    
    /**
     * Verifies that user is a member of the workspace
     */
    private void verifyWorkspaceMembership(UserEntity user, BoardEntity board) 
            throws BoardNotFoundException {
        boolean isWorkspaceMember = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(board.getWorkspace().getId(), user.getId())
                .isPresent();
                
        if (!isWorkspaceMember) {
            throw new BoardNotFoundException("User is not a member of this board and has no access");
        }
    }
    
    /**
     * Creates board membership for user with specified role
     */
    private void createBoardMembership(BoardEntity board, UserEntity user, BoardRoleEntity role) {
        BoardMemberEntity newMember = BoardMemberEntity.builder()
                .board(board)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
                
        boardMemberRepository.save(newMember);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canInviteBoardMembers(#p1, authentication)")
    public BoardMemberRead createMember(UserEntity user, Long boardId, BoardMemberCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

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
    @PreAuthorize("@featureAuthService.canManageBoardRoles(#p1, authentication)")
    public BoardMemberRead updateMember(UserEntity user, Long boardId, Long memberId, BoardMemberUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
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
    @PreAuthorize("@featureAuthService.canInviteBoardMembers(#p1, authentication)")
    public BoardMemberRead deleteMember(UserEntity user, Long boardId, Long memberId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardMemberEntity member = boardMemberRepository.findById(memberId)
            .orElseThrow(() -> new BoardNotFoundException("Board member not found with id: " + memberId));
            
        if (!member.getBoard().getId().equals(boardId)) {
            throw new BoardNotFoundException("Board member does not belong to this board");
        }
        
        boardMemberRepository.delete(member);
        
        return boardMemberMapper.toDto(member);
    }
}
