package com.kyut.ordo.feature.board;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.dto.BoardCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberRead;
import com.kyut.ordo.feature.board.dto.BoardMemberUpdate;
import com.kyut.ordo.feature.board.dto.BoardRead;
import com.kyut.ordo.feature.board.dto.BoardRoleCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleRead;
import com.kyut.ordo.feature.board.dto.BoardRoleUpdate;
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
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.board.service.BoardRoleFactory;
import com.kyut.ordo.feature.board.service.BoardService;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BoardService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Unit Tests")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BoardMemberRepository boardMemberRepository;
    @Mock
    private BoardRoleRepository boardRoleRepository;
    @Mock
    private BoardPermissionService boardPermissionService;
    @Mock
    private BoardMapper boardMapper;
    @Mock
    private BoardRoleMapper boardRoleMapper;
    @Mock
    private BoardMemberMapper boardMemberMapper;
    @Mock
    private BoardRoleFactory boardRoleFactory;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardService boardService;

    private UserEntity testUser;
    private BoardEntity testBoard;
    private WorkspaceEntity testWorkspace;
    private BoardCreate boardCreateDto;
    private BoardRead boardReadDto;
    private BoardRoleEntity testRole;
    private BoardMemberEntity testMember;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");

        testWorkspace = new WorkspaceEntity();
        testWorkspace.setId(1L);
        testWorkspace.setTitle("Test Workspace");

        testBoard = new BoardEntity();
        testBoard.setId(1L);
        testBoard.setTitle("Test Board");
        testBoard.setDescription("Test Description");
        testBoard.setVisibility(BoardVisibility.PRIVATE);
        testBoard.setWorkspace(testWorkspace);
        testBoard.setCreatedAt(LocalDateTime.now());

        boardCreateDto = new BoardCreate();
        boardCreateDto.setWorkspaceId(1L);
        boardCreateDto.setTitle("Test Board");
        boardCreateDto.setDescription("Test Description");
        boardCreateDto.setVisibility(BoardVisibility.PRIVATE);

        boardReadDto = new BoardRead();
        boardReadDto.setId(1L);
        boardReadDto.setTitle("Test Board");
        boardReadDto.setDescription("Test Description");

        testRole = new BoardRoleEntity();
        testRole.setId(1L);
        testRole.setName("Admin");
        testRole.setBoard(testBoard);

        testMember = new BoardMemberEntity();
        testMember.setId(1L);
        testMember.setUser(testUser);
        testMember.setBoard(testBoard);
        testMember.setRole(testRole);
    }

    @Test
    @DisplayName("Find board by ID - success")
    void findById_Success() throws BoardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(true);
        when(boardMapper.toDto(testBoard)).thenReturn(boardReadDto);

        // When
        BoardRead result = boardService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Board");
        verify(boardRepository).findById(1L);
        verify(boardMapper).toDto(testBoard);
    }

    @Test
    @DisplayName("Find board by ID - board not found")
    void findById_BoardNotFound() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> boardService.findById(testUser, 1L))
            .isInstanceOf(BoardNotFoundException.class)
            .hasMessageContaining("Board not found with id: 1");
    }

    @Test
    @DisplayName("Find board by ID - insufficient permissions")
    void findById_InsufficientPermissions() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> boardService.findById(testUser, 1L))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have access to this board");
    }

    @Test
    @DisplayName("Create board - success")
    void createBoard_Success() throws InsufficientBoardPermissionsException {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_BOARDS")).thenReturn(true);
        when(boardMapper.toEntity(boardCreateDto, testWorkspace)).thenReturn(testBoard);
        when(boardRepository.save(testBoard)).thenReturn(testBoard);
        when(boardRoleFactory.createOwnerRole(testBoard)).thenReturn(testRole);
        when(boardRoleRepository.save(testRole)).thenReturn(testRole);
        when(boardMemberRepository.save(any(BoardMemberEntity.class))).thenReturn(testMember);
        when(boardMapper.toDto(testBoard)).thenReturn(boardReadDto);

        // When
        BoardRead result = boardService.createBoard(testUser, boardCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Board");
        verify(boardRepository).save(testBoard);
        verify(boardRoleRepository).save(testRole);
        verify(boardMemberRepository).save(any(BoardMemberEntity.class));
    }

    @Test
    @DisplayName("Create board - insufficient workspace permissions")
    void createBoard_InsufficientPermissions() {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_BOARDS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> boardService.createBoard(testUser, boardCreateDto))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to create boards in this workspace");
    }

    @Test
    @DisplayName("Find all accessible boards - success")
    void findAllAccessibleBoards_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<BoardEntity> boards = List.of(testBoard);
        Page<BoardEntity> boardPage = new PageImpl<>(boards, pageable, 1);
        
        when(boardRepository.findAll(pageable)).thenReturn(boardPage);
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(true);
        when(boardMapper.toDto(testBoard)).thenReturn(boardReadDto);

        // When
        Page<BoardRead> result = boardService.findAllAccessibleBoards(testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Board");
        verify(boardRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Find boards by workspace - success")
    void findAllBoardsByWorkspace_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<BoardEntity> boards = List.of(testBoard);
        Page<BoardEntity> boardPage = new PageImpl<>(boards, pageable, 1);
        
        when(boardRepository.findAllByWorkspaceId(1L, pageable)).thenReturn(boardPage);
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(true);
        when(boardMapper.toDto(testBoard)).thenReturn(boardReadDto);

        // When
        Page<BoardRead> result = boardService.findAllBoardsByWorkspace(testUser, 1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Board");
        verify(boardRepository).findAllByWorkspaceId(1L, pageable);
    }

    @Test
    @DisplayName("Delete board - success")
    void deleteBoard_Success() throws BoardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "DELETE")).thenReturn(true);
        when(boardMapper.toDto(testBoard)).thenReturn(boardReadDto);

        // When
        BoardRead result = boardService.deleteBoard(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(boardRepository).findById(1L);
        verify(boardRepository).delete(testBoard);
    }

    @Test
    @DisplayName("Delete board - board not found")
    void deleteBoard_BoardNotFound() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> boardService.deleteBoard(testUser, 1L))
            .isInstanceOf(BoardNotFoundException.class)
            .hasMessageContaining("Board not found with id: 1");
    }

    @Test
    @DisplayName("Delete board - insufficient permissions")
    void deleteBoard_InsufficientPermissions() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "DELETE")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> boardService.deleteBoard(testUser, 1L))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to delete this board");
    }

    @Test
    @DisplayName("Add member to board - success")
    void addMember_Success() throws InsufficientBoardPermissionsException {
        // Given
        UserEntity memberUser = TestConfig.TestDataFactory.createTestUserWithId(2L, "member@example.com", "Member User");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_MEMBERS")).thenReturn(true);
        // Mock role lookup by name - simplified for test
        when(boardMemberRepository.save(any(BoardMemberEntity.class))).thenReturn(testMember);

        // When
        boardService.addMember(testUser, 1L, 2L, testRole.getId());

        // Then
        verify(boardRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(boardMemberRepository).save(any(BoardMemberEntity.class));
    }

    @Test
    @DisplayName("Add member to board - insufficient permissions")
    void addMember_InsufficientPermissions() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_MEMBERS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> boardService.addMember(testUser, 1L, 2L, testRole.getId()))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to manage members of this board");
    }

    @Test
    @DisplayName("Get user role in board - success")
    void getMyRole_Success() throws BoardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        BoardRoleRead roleRead = new BoardRoleRead();
        roleRead.setId(1L);
        roleRead.setName("Admin");

        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(true);
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId())).thenReturn(Optional.of(testMember));
        when(boardRoleMapper.toDto(testRole)).thenReturn(roleRead);

        // When
        BoardRoleRead result = boardService.getMyRole(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Admin");
        verify(boardRepository).findById(1L);
        verify(boardMemberRepository).findByBoardIdAndUserId(1L, testUser.getId());
    }

    @Test
    @DisplayName("Get user role in board - user not a member")
    void getMyRole_UserNotMember() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "VIEW")).thenReturn(true);
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> boardService.getMyRole(testUser, 1L))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User is not a member of this board");
    }
}
