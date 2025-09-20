package com.kyut.ordo.feature.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.dto.BoardCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleUpdate;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import com.kyut.ordo.testcontainers.PostgreSQLTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BoardController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("BoardController Integration Tests with PostgreSQL")
@Transactional
class BoardControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private BoardRoleRepository boardRoleRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private WorkspaceRoleRepository workspaceRoleRepository;

    @Autowired
    private PostgreSQLTestDataBuilder testDataBuilder;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private BoardRoleEntity adminRole;
    private BoardRoleEntity memberRole;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test users
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        otherUser = dataBuilder.createTestUser("other@example.com", "Other User");

        // Create test workspace
        testWorkspace = createTestWorkspace();

        // Create test board with user as admin
        testBoard = createTestBoard();
        setupBoardRolesAndMembership();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create board - success scenario")
    void createBoard_Success() throws Exception {
        // Given

        BoardCreate boardCreate = new BoardCreate();
        boardCreate.setWorkspaceId(testWorkspace.getId());
        boardCreate.setTitle("New Board");
        boardCreate.setDescription("New Description");
        boardCreate.setVisibility(BoardVisibility.PRIVATE);

        // When & Then
        mockMvc.perform(post("/boards")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Board"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.workspace.id").value(testWorkspace.getId()));

        // Verify board was created in database
        List<BoardEntity> boards = (List<BoardEntity>) boardRepository.findAll();
        assertThat(boards).hasSize(2); // testBoard + new board
        assertThat(boards).anyMatch(board -> "New Board".equals(board.getTitle()));
    }

    @Test
    @DisplayName("Create board - unauthorized user")
    void createBoard_Unauthorized() throws Exception {
        // Given
        BoardCreate boardCreate = new BoardCreate();
        boardCreate.setWorkspaceId(testWorkspace.getId());
        boardCreate.setTitle("Unauthorized Board");

        // When & Then
        mockMvc.perform(post("/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardCreate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get all accessible boards - success")
    void getAllAccessibleBoards_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards")
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Board"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"));
    }

    @Test
    @DisplayName("Get board by ID - success")
    void getBoardById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}", testBoard.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBoard.getId()))
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Get board by ID - not found")
    void getBoardById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get board by ID - insufficient permissions")
    void getBoardById_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}", testBoard.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete board - success")
    void deleteBoard_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/boards/{boardId}", testBoard.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBoard.getId()));

        // Verify board is deleted
        assertThat(boardRepository.findById(testBoard.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete board - insufficient permissions")
    void deleteBoard_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(delete("/boards/{boardId}", testBoard.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());

        // Verify board still exists
        assertThat(boardRepository.findById(testBoard.getId())).isPresent();
    }

    @Test
    @DisplayName("Add member to board - success")
    void addMemberToBoard_Success() throws Exception {
        // Given
        BoardMemberCreate memberCreate = new BoardMemberCreate();
        memberCreate.setUserId(otherUser.getId());
        memberCreate.setBoardRoleId(memberRole.getId());

        // When & Then
        mockMvc.perform(post("/boards/{boardId}/members", testBoard.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberCreate)))
                .andExpect(status().isOk());

        // Verify member was added
        assertThat(boardMemberRepository.findByBoardIdAndUserId(testBoard.getId(), otherUser.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("Get board members - success")
    void getBoardMembers_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}/members", testBoard.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Create board role - success")
    void createBoardRole_Success() throws Exception {
        // Given
        BoardRoleCreate roleCreate = new BoardRoleCreate();
        roleCreate.setBoardId(testBoard.getId());
        roleCreate.setName("Viewer");

        // When & Then
        mockMvc.perform(post("/boards/{boardId}/roles", testBoard.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Viewer"));

        // Verify role was created by checking repository
        List<BoardRoleEntity> roles = (List<BoardRoleEntity>) boardRoleRepository.findAll();
        assertThat(roles).anyMatch(role -> "Viewer".equals(role.getName()));
    }

    @Test
    @DisplayName("Update board role - success")
    void updateBoardRole_Success() throws Exception {
        // Given
        BoardRoleUpdate roleUpdate = new BoardRoleUpdate();
        roleUpdate.setName("Super Member");

        // When & Then
        mockMvc.perform(put("/boards/{boardId}/roles/{roleId}", testBoard.getId(), memberRole.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Super Member"));

        // Verify role was updated
        BoardRoleEntity updatedRole = boardRoleRepository.findById(memberRole.getId()).orElseThrow();
        assertThat(updatedRole.getName()).isEqualTo("Super Member");
    }

    @Test
    @DisplayName("Get board roles - success")
    void getBoardRoles_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}/roles", testBoard.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Admin')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Member')]").exists());
    }

    @Test
    @DisplayName("Get my role in board - success")
    void getMyRole_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/boards/{boardId}/my-role", testBoard.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    private WorkspaceEntity createTestWorkspace() {

        WorkspaceEntity workspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);

//        WorkspaceEntity workspace = new WorkspaceEntity();
//        workspace.setTitle("Test Workspace");
//        workspace.setDescription("Test Description");
//        workspace.setCreatedAt(LocalDateTime.now());
//        workspace.setOwner(testUser);
//        workspace = workspaceRepository.save(workspace);

        // Create workspace roles and add user as admin
//        WorkspaceRoleEntity adminRole = new WorkspaceRoleEntity();
//        adminRole.setName("Admin");
//        adminRole.setWorkspace(workspace);
        // Note: WorkspaceRoleEntity permission structure might be different
//        workspaceRoleRepository.save(adminRole);

//        WorkspaceMemberEntity member = new WorkspaceMemberEntity();
//        member.setUser(testUser);
//        member.setWorkspace(workspace);
//        member.setRole(adminRole);
//        member.setJoinedAt(LocalDateTime.now());
//        workspaceMemberRepository.save(member);

        return workspace;
    }

    private BoardEntity createTestBoard() {
        BoardEntity board = new BoardEntity();
        board.setTitle("Test Board");
        board.setDescription("Test Description");
        board.setVisibility(BoardVisibility.PRIVATE);
        board.setWorkspace(testWorkspace);
        board.setCreatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    private void setupBoardRolesAndMembership() {
        // Create admin role
        adminRole = new BoardRoleEntity();
        adminRole.setName("Admin");
        adminRole.setBoard(testBoard);
        adminRole.setAbleToEdit(true);
        adminRole.setAbleToDelete(true);
        adminRole.setAbleToInviteMembers(true);
        adminRole.setAbleToManageRoles(true);
        adminRole.setAbleToCreateLists(true);
        adminRole.setAbleToCreateTasks(true);
        adminRole = boardRoleRepository.save(adminRole);

        // Create member role
        memberRole = new BoardRoleEntity();
        memberRole.setName("Member");
        memberRole.setBoard(testBoard);
        memberRole.setAbleToEdit(false);
        memberRole.setAbleToDelete(false);
        memberRole.setAbleToInviteMembers(false);
        memberRole.setAbleToManageRoles(false);
        memberRole.setAbleToCreateLists(true);
        memberRole.setAbleToCreateTasks(true);
        memberRole = boardRoleRepository.save(memberRole);

        // Add test user as admin
        BoardMemberEntity adminMember = new BoardMemberEntity();
        adminMember.setUser(testUser);
        adminMember.setBoard(testBoard);
        adminMember.setRole(adminRole);
        adminMember.setJoinedAt(LocalDateTime.now());
        boardMemberRepository.save(adminMember);
    }
}