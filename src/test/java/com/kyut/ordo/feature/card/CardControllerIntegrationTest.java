package com.kyut.ordo.feature.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.card.dto.CardCreate;
import com.kyut.ordo.feature.card.dto.CardPositionUpdate;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.repository.CardRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CardController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("CardController Integration Tests with PostgreSQL")
@Transactional
class CardControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CardRepository cardRepository;


    @Autowired
    private BoardMemberRepository boardMemberRepository;

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

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create card - success scenario")
    void createCard_Success() throws Exception {
        // Given
        CardCreate cardCreate = new CardCreate();
        cardCreate.setTitle("New Card");
        cardCreate.setDescription("New Description");
        cardCreate.setDueDate(LocalDate.now().plusDays(7));
        cardCreate.setListId(testList.getId());
        cardCreate.setAssignedToId(otherUser.getId());

        // When & Then
        mockMvc.perform(post("/cards")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Card"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.list.id").value(testList.getId()));

        // Verify card was created in database
        List<CardEntity> cards = (List<CardEntity>) cardRepository.findAll();
        assertThat(cards).hasSize(2); // testCard + new card
        assertThat(cards).anyMatch(card -> "New Card".equals(card.getTitle()));
    }

    @Test
    @DisplayName("Create card - unauthorized user")
    void createCard_Unauthorized() throws Exception {
        // Given
        CardCreate cardCreate = new CardCreate();
        cardCreate.setTitle("Unauthorized Card");
        cardCreate.setListId(testList.getId());

        // When & Then
        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardCreate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get all cards assigned to user - success")
    void getAllAssignedToUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/assigned")
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Card"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"));
    }

    @Test
    @DisplayName("Get card by ID - success")
    void getCardById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/{cardId}", testCard.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCard.getId()))
                .andExpect(jsonPath("$.title").value("Test Card"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Get card by ID - not found")
    void getCardById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/{cardId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get card by ID - insufficient permissions")
    void getCardById_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/{cardId}", testCard.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update card - success")
    void updateCard_Success() throws Exception {
        // Given
        CardCreate cardUpdate = new CardCreate();
        cardUpdate.setTitle("Updated Card");
        cardUpdate.setDescription("Updated Description");
        cardUpdate.setListId(testList.getId());

        // When & Then
        mockMvc.perform(put("/cards/{cardId}", testCard.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Card"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Verify card was updated in database
        Optional<CardEntity> updatedCard = cardRepository.findById(testCard.getId());
        assertThat(updatedCard).isPresent();
        assertThat(updatedCard.get().getTitle()).isEqualTo("Updated Card");
    }

    @Test
    @DisplayName("Update card - insufficient permissions")
    void updateCard_InsufficientPermissions() throws Exception {
        // Given
        CardCreate cardUpdate = new CardCreate();
        cardUpdate.setTitle("Unauthorized Update");
        cardUpdate.setListId(testList.getId());

        // When & Then
        mockMvc.perform(put("/cards/{cardId}", testCard.getId())
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete card - success")
    void deleteCard_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/cards/{cardId}", testCard.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCard.getId()));

        // Verify card is deleted
        assertThat(cardRepository.findById(testCard.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete card - insufficient permissions")
    void deleteCard_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(delete("/cards/{cardId}", testCard.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());

        // Verify card still exists
        assertThat(cardRepository.findById(testCard.getId())).isPresent();
    }

    @Test
    @DisplayName("Update card positions - success")
    void updateCardPositions_Success() throws Exception {
        // Given
        CardEntity secondCard = createSecondTestCard();
        CardPositionUpdate positionUpdate = new CardPositionUpdate();
        positionUpdate.setListId(testList.getId());
        positionUpdate.setCardIds(List.of(secondCard.getId(), testCard.getId()));

        // When & Then
        mockMvc.perform(put("/cards/positions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(positionUpdate)))
                .andExpect(status().isOk());

        // Verify positions were updated
        CardEntity firstCard = cardRepository.findById(secondCard.getId()).orElseThrow();
        CardEntity secondUpdatedCard = cardRepository.findById(testCard.getId()).orElseThrow();
        assertThat(firstCard.getPosition()).isZero();
        assertThat(secondUpdatedCard.getPosition()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get card comments - success")
    void getCardComments_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/{cardId}/comments", testCard.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get card tasks - success")
    void getCardTasks_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/cards/{cardId}/tasks", testCard.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private CardEntity createTestCard() {
        CardEntity card = new CardEntity();
        card.setTitle("Test Card");
        card.setDescription("Test Description");
        card.setDueDate(LocalDate.now().plusDays(7));
        card.setPosition(0);
        card.setList(testList);
        card.setCreatedBy(testUser);
        card.setAssignedTo(testUser);
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    private CardEntity createSecondTestCard() {
        CardEntity card = new CardEntity();
        card.setTitle("Second Test Card");
        card.setDescription("Second Test Description");
        card.setDueDate(LocalDate.now().plusDays(14));
        card.setPosition(1);
        card.setList(testList);
        card.setCreatedBy(testUser);
        card.setAssignedTo(otherUser);
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
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
