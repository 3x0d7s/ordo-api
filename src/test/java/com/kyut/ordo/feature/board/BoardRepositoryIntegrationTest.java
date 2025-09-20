package com.kyut.ordo.feature.board;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import com.kyut.ordo.testcontainers.PostgreSQLTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BoardRepository using PostgreSQL with Testcontainers
 */
@Import(TestConfig.class)
@DisplayName("BoardRepository Integration Tests with PostgreSQL")
@Transactional
class BoardRepositoryIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private BoardRepository boardRepository;
    
    @Autowired
    private BoardMemberRepository boardMemberRepository;
    
    @Autowired
    private BoardRoleRepository boardRoleRepository;
    
    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    private UserEntity testUser;
    private WorkspaceEntity testWorkspace;
    private WorkspaceEntity otherWorkspace;
    private BoardEntity testBoard1;
    private BoardEntity testBoard2;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        
        // Create workspaces manually since dataBuilder might create boards automatically
        testWorkspace = new WorkspaceEntity();
        testWorkspace.setTitle("Test Workspace");
        testWorkspace.setDescription("Test Description");
        testWorkspace.setCreatedAt(LocalDateTime.now());
        testWorkspace.setOwner(testUser);
        testWorkspace = workspaceRepository.save(testWorkspace);

        otherWorkspace = new WorkspaceEntity();
        otherWorkspace.setTitle("Other Workspace");
        otherWorkspace.setDescription("Other Description");
        otherWorkspace.setCreatedAt(LocalDateTime.now());
        otherWorkspace.setOwner(testUser);
        otherWorkspace = workspaceRepository.save(otherWorkspace);

        testBoard1 = createBoard("Board 1", "Description 1", BoardVisibility.PRIVATE, testWorkspace);
        testBoard2 = createBoard("Board 2", "Description 2", BoardVisibility.PUBLIC, testWorkspace);
    }

    @Test
    @DisplayName("Find all boards with pagination")
    void findAll_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BoardEntity> result = boardRepository.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
            .extracting(BoardEntity::getTitle)
            .containsExactlyInAnyOrder("Board 1", "Board 2");
    }

    @Test
    @DisplayName("Find boards by workspace ID")
    void findAllByWorkspaceId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BoardEntity> result = boardRepository.findAllByWorkspaceId(testWorkspace.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(BoardEntity::getTitle)
            .containsExactlyInAnyOrder("Board 1", "Board 2");
        
        // All boards should belong to testWorkspace
        assertThat(result.getContent())
            .allMatch(board -> board.getWorkspace().getId().equals(testWorkspace.getId()));
    }

    @Test
    @DisplayName("Find boards by workspace ID - no boards")
    void findAllByWorkspaceId_NoBoards() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search in workspace with no boards
        Page<BoardEntity> result = boardRepository.findAllByWorkspaceId(otherWorkspace.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Find board by ID")
    void findById_Success() {
        // When
        Optional<BoardEntity> result = boardRepository.findById(testBoard1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Board 1");
        assertThat(result.get().getDescription()).isEqualTo("Description 1");
        assertThat(result.get().getVisibility()).isEqualTo(BoardVisibility.PRIVATE);
        assertThat(result.get().getWorkspace()).isEqualTo(testWorkspace);
    }

    @Test
    @DisplayName("Find board by ID - not found")
    void findById_NotFound() {
        // When
        Optional<BoardEntity> result = boardRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Save board")
    void save_Success() {
        // Given
        BoardEntity newBoard = new BoardEntity();
        newBoard.setTitle("New Board");
        newBoard.setDescription("New Description");
        newBoard.setVisibility(BoardVisibility.PUBLIC);
        newBoard.setWorkspace(testWorkspace);
        newBoard.setCreatedAt(LocalDateTime.now());

        // When
        BoardEntity savedBoard = boardRepository.save(newBoard);

        // Then
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getId()).isNotNull();
        assertThat(savedBoard.getTitle()).isEqualTo("New Board");
        assertThat(savedBoard.getDescription()).isEqualTo("New Description");
        assertThat(savedBoard.getVisibility()).isEqualTo(BoardVisibility.PUBLIC);
        assertThat(savedBoard.getWorkspace()).isEqualTo(testWorkspace);
        assertThat(savedBoard.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Update board")
    void update_Success() {
        // Given
        testBoard1.setTitle("Updated Board Title");
        testBoard1.setDescription("Updated Description");
        testBoard1.setVisibility(BoardVisibility.PUBLIC);

        // When
        BoardEntity updatedBoard = boardRepository.save(testBoard1);

        // Then
        assertThat(updatedBoard).isNotNull();
        assertThat(updatedBoard.getTitle()).isEqualTo("Updated Board Title");
        assertThat(updatedBoard.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedBoard.getVisibility()).isEqualTo(BoardVisibility.PUBLIC);

        // Verify in database
        Optional<BoardEntity> fromDb = boardRepository.findById(testBoard1.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getTitle()).isEqualTo("Updated Board Title");
    }

    @Test
    @DisplayName("Delete board")
    void delete_Success() {
        // Given
        Long boardId = testBoard1.getId();

        // When
        boardRepository.delete(testBoard1);

        // Then
        Optional<BoardEntity> result = boardRepository.findById(boardId);
        assertThat(result).isEmpty();

        // Verify other board still exists
        Optional<BoardEntity> otherBoard = boardRepository.findById(testBoard2.getId());
        assertThat(otherBoard).isPresent();
    }

    @Test
    @DisplayName("Filter boards by visibility")
    void filterBoardsByVisibility() {
        // When - get all boards and filter manually
        List<BoardEntity> allBoards = (List<BoardEntity>) boardRepository.findAll();
        List<BoardEntity> publicBoards = allBoards.stream()
            .filter(board -> board.getVisibility() == BoardVisibility.PUBLIC)
            .toList();
        List<BoardEntity> privateBoards = allBoards.stream()
            .filter(board -> board.getVisibility() == BoardVisibility.PRIVATE)
            .toList();

        // Then
        assertThat(publicBoards).hasSize(1);
        assertThat(publicBoards.get(0).getTitle()).isEqualTo("Board 2");

        assertThat(privateBoards).hasSize(1);
        assertThat(privateBoards.get(0).getTitle()).isEqualTo("Board 1");
    }

    @Test
    @DisplayName("Board with members and roles relationship")
    void boardWithMembersAndRoles() {
        // Given - Create role and member
        BoardRoleEntity adminRole = new BoardRoleEntity();
        adminRole.setName("Admin");
        adminRole.setBoard(testBoard1);
        adminRole.setAbleToEdit(true);
        adminRole.setAbleToDelete(true);
        adminRole.setAbleToInviteMembers(true);
        adminRole.setAbleToManageRoles(true);
        adminRole.setAbleToCreateLists(true);
        adminRole.setAbleToCreateTasks(true);
        adminRole = boardRoleRepository.save(adminRole);

        BoardMemberEntity member = new BoardMemberEntity();
        member.setUser(testUser);
        member.setBoard(testBoard1);
        member.setRole(adminRole);
        member.setJoinedAt(LocalDateTime.now());
        boardMemberRepository.save(member);

        // When
        Optional<BoardEntity> boardWithRelations = boardRepository.findById(testBoard1.getId());

        // Then
        assertThat(boardWithRelations).isPresent();
        BoardEntity board = boardWithRelations.get();
        assertThat(board.getTitle()).isEqualTo("Board 1");
        
        // Verify member was created successfully
        List<BoardMemberEntity> members = boardMemberRepository.findAllByBoardId(testBoard1.getId());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUser()).isEqualTo(testUser);
        assertThat(members.get(0).getRole().getName()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("Test workspace relationship")
    void testWorkspaceRelationship() {
        // When
        Optional<BoardEntity> board = boardRepository.findById(testBoard1.getId());

        // Then
        assertThat(board).isPresent();
        assertThat(board.get().getWorkspace()).isNotNull();
        assertThat(board.get().getWorkspace().getTitle()).isEqualTo("Test Workspace");
        assertThat(board.get().getWorkspace().getId()).isEqualTo(testWorkspace.getId());
    }

    @Test
    @DisplayName("Count boards by workspace manually")
    void countBoardsByWorkspace() {
        // When
        List<BoardEntity> allBoards = (List<BoardEntity>) boardRepository.findAll();
        long testWorkspaceBoards = allBoards.stream()
            .filter(board -> board.getWorkspace().getId().equals(testWorkspace.getId()))
            .count();
        long otherWorkspaceBoards = allBoards.stream()
            .filter(board -> board.getWorkspace().getId().equals(otherWorkspace.getId()))
            .count();

        // Then
        assertThat(testWorkspaceBoards).isEqualTo(2);
        assertThat(otherWorkspaceBoards).isEqualTo(0);
    }

    private BoardEntity createBoard(String title, String description, BoardVisibility visibility, WorkspaceEntity workspace) {
        BoardEntity board = new BoardEntity();
        board.setTitle(title);
        board.setDescription(description);
        board.setVisibility(visibility);
        board.setWorkspace(workspace);
        board.setCreatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }
}