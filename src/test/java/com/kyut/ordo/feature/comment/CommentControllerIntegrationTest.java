package com.kyut.ordo.feature.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.comment.dto.CommentCreate;
import com.kyut.ordo.feature.comment.entity.CommentEntity;
import com.kyut.ordo.feature.comment.repository.CommentRepository;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CommentController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("CommentController Integration Tests with PostgreSQL")
@Transactional
class CommentControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardRoleRepository boardRoleRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private CommentEntity testComment;
    private BoardRoleEntity adminRole;
    private BoardRoleEntity memberRole;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test users
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        otherUser = dataBuilder.createTestUser("other@example.com", "Other User");

        // Create test workspace
        testWorkspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);

        // Create test board with user as admin
        testBoard = dataBuilder.createTestBoardWithWorkspace("Test Board", "Test Description", testUser, testWorkspace);

        // Create test list
        testList = dataBuilder.createTestList("Test List", 0, testBoard);

        // Setup additional roles (board already has user as owner from dataBuilder)
        setupBoardRoles();

        // Create test card
        testCard = createTestCard();

        // Create test comment
        testComment = createTestComment();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create comment - success scenario")
    void createComment_Success() throws Exception {
        // Given
        CommentCreate commentCreate = new CommentCreate();
        commentCreate.setMessage("New comment message");
        commentCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/comments")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New comment message"))
                .andExpect(jsonPath("$.card.id").value(testCard.getId()));

        // Verify comment was created in database
        List<CommentEntity> comments = (List<CommentEntity>) commentRepository.findAll();
        assertThat(comments).hasSize(2); // testComment + new comment
        assertThat(comments).anyMatch(comment -> "New comment message".equals(comment.getMessage()));
    }

    @Test
    @DisplayName("Create comment - unauthorized user")
    void createComment_Unauthorized() throws Exception {
        // Given
        CommentCreate commentCreate = new CommentCreate();
        commentCreate.setMessage("Unauthorized comment");
        commentCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentCreate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create comment - insufficient permissions")
    void createComment_InsufficientPermissions() throws Exception {
        // Given
        CommentCreate commentCreate = new CommentCreate();
        commentCreate.setMessage("Forbidden comment");
        commentCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/comments")
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentCreate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create comment - card not found")
    void createComment_CardNotFound() throws Exception {
        // Given
        CommentCreate commentCreate = new CommentCreate();
        commentCreate.setMessage("Comment for non-existent card");
        commentCreate.setCardId(999L);

        // When & Then
        mockMvc.perform(post("/comments")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentCreate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get comment by ID - success")
    void getCommentById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/comments/{commentId}", testComment.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testComment.getId()))
                .andExpect(jsonPath("$.message").value("Test Comment Message"));
    }

    @Test
    @DisplayName("Get comment by ID - not found")
    void getCommentById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/comments/{commentId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get comment by ID - insufficient permissions")
    void getCommentById_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(get("/comments/{commentId}", testComment.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all comments by user - success")
    void getAllCommentsByUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/comments/user")
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].message").value("Test Comment Message"));
    }

    @Test
    @DisplayName("Update comment - success by comment creator")
    void updateComment_SuccessByCreator() throws Exception {
        // Given
        CommentCreate commentUpdate = new CommentCreate();
        commentUpdate.setMessage("Updated comment message");
        commentUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/comments/{commentId}", testComment.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated comment message"));

        // Verify comment was updated in database
        Optional<CommentEntity> updatedComment = commentRepository.findById(testComment.getId());
        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().getMessage()).isEqualTo("Updated comment message");
    }

    @Test
    @DisplayName("Update comment - not found")
    void updateComment_NotFound() throws Exception {
        // Given
        CommentCreate commentUpdate = new CommentCreate();
        commentUpdate.setMessage("Updated comment message");
        commentUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/comments/{commentId}", 999L)
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update comment - insufficient permissions")
    void updateComment_InsufficientPermissions() throws Exception {
        // Given
        CommentCreate commentUpdate = new CommentCreate();
        commentUpdate.setMessage("Unauthorized update");
        commentUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/comments/{commentId}", testComment.getId())
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete comment - success by comment creator")
    void deleteComment_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/comments/{commentId}", testComment.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testComment.getId()));

        // Verify comment is deleted
        assertThat(commentRepository.findById(testComment.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete comment - not found")
    void deleteComment_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/comments/{commentId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete comment - insufficient permissions")
    void deleteComment_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(delete("/comments/{commentId}", testComment.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());

        // Verify comment still exists
        assertThat(commentRepository.findById(testComment.getId())).isPresent();
    }

    private CardEntity createTestCard() {
        CardEntity card = new CardEntity();
        card.setTitle("Test Card");
        card.setDescription("Test Card Description");
        card.setPosition(0);
        card.setList(testList);
        card.setCreatedBy(testUser);
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    private CommentEntity createTestComment() {
        CommentEntity comment = new CommentEntity();
        comment.setMessage("Test Comment Message");
        comment.setCreatedBy(testUser);
        comment.setCard(testCard);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private void setupBoardRoles() {
        // Create admin role (user is already added as Owner by dataBuilder)
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
    }
}
