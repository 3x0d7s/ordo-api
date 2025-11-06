package com.kyut.ordo.feature.list;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.list.dto.ListCreate;
import com.kyut.ordo.feature.list.dto.ListPositionUpdate;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ListController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("ListController Integration Tests with PostgreSQL")
@Transactional
class ListControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private BoardEntity testBoard;
    private ListEntity testList;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test user
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");

        // Create test board with user as owner (this will also create board roles and membership)
        testBoard = dataBuilder.createTestBoardWithOwner("Test Board", "Test Description", testUser);

        // Create test list
        testList = dataBuilder.createTestList("Test List", 0, testBoard);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create list - success scenario")
    void createList_Success() throws Exception {
        // Given
        ListCreate listCreate = new ListCreate();
        listCreate.setTitle("New List");
        listCreate.setPosition(1);
        listCreate.setColor("#FF5733");
        listCreate.setBoardId(testBoard.getId());

        // When & Then
        mockMvc.perform(post("/lists")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New List"))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.color").value("#FF5733"))
                .andExpect(jsonPath("$.board.id").value(testBoard.getId()));

        // Verify list was saved in database
        assertThat(listRepository.findAllByBoardOrderByPosition(testBoard)).hasSize(2);
    }

    @Test
    @DisplayName("Create list - validation error for empty title")
    void createList_ValidationError() throws Exception {
        // Given
        ListCreate listCreate = new ListCreate();
        listCreate.setTitle(""); // Empty title
        listCreate.setBoardId(testBoard.getId());

        // When & Then
        mockMvc.perform(post("/lists")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listCreate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get list by ID - success scenario")
    void getListById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/lists/{id}", testList.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testList.getId()))
                .andExpect(jsonPath("$.title").value("Test List"))
                .andExpect(jsonPath("$.position").value(0))
                .andExpect(jsonPath("$.board.id").value(testBoard.getId()));
    }

    @Test
    @DisplayName("Get list by ID - list not found")
    void getListById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/lists/{id}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update list - success scenario")
    void updateList_Success() throws Exception {
        // Given
        ListCreate listUpdate = new ListCreate();
        listUpdate.setTitle("Updated List");
        listUpdate.setPosition(1);
        listUpdate.setColor("#00FF00");
        listUpdate.setBoardId(testBoard.getId());

        // When & Then
        mockMvc.perform(put("/lists/{id}", testList.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated List"))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.color").value("#00FF00"));

        // Verify list was updated in database
        ListEntity updatedList = listRepository.findById(testList.getId()).orElseThrow();
        assertThat(updatedList.getTitle()).isEqualTo("Updated List");
        assertThat(updatedList.getPosition()).isEqualTo(1);
    }

    @Test
    @DisplayName("Delete list - success scenario")
    void deleteList_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/lists/{id}", testList.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testList.getId()))
                .andExpect(jsonPath("$.title").value("Test List"));

        // Verify list was deleted from database
        assertThat(listRepository.findById(testList.getId())).isEmpty();
    }

    @Test
    @DisplayName("Update list positions - success scenario")
    void updateListPositions_Success() throws Exception {
        // Given
        ListEntity list1 = dataBuilder.createTestList("List 1", 0, testBoard);
        ListEntity list2 = dataBuilder.createTestList("List 2", 1, testBoard);
        ListEntity list3 = dataBuilder.createTestList("List 3", 2, testBoard);

        ListPositionUpdate positionUpdate = new ListPositionUpdate();
        positionUpdate.setBoardId(testBoard.getId());
        positionUpdate.setListIds(Arrays.asList(list3.getId(), list1.getId(), list2.getId(), testList.getId())); // Reorder

        // When & Then
        mockMvc.perform(put("/lists/positions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(positionUpdate)))
                .andExpect(status().isOk());

        // Verify positions were updated in database
        var lists = listRepository.findAllByBoardOrderByPosition(testBoard);
        assertThat(lists).hasSize(4); // 3 new + 1 original
        assertThat(lists.get(0).getId()).isEqualTo(list3.getId()); // First position
        assertThat(lists.get(1).getId()).isEqualTo(list1.getId()); // Second position
        assertThat(lists.get(2).getId()).isEqualTo(list2.getId()); // Third position
    }

    @Test
    @DisplayName("Get cards by list ID - success scenario")
    void getCardsByListId_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/lists/{id}/cards", testList.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    @DisplayName("Access without authentication - unauthorized")
    void accessWithoutAuthentication_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/lists/{id}", testList.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ListCreate())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create list with non-existent board - not found")
    void createListWithNonExistentBoard_NotFound() throws Exception {
        // Given
        ListCreate listCreate = new ListCreate();
        listCreate.setTitle("New List");
        listCreate.setBoardId(999L); // Non-existent board

        // When & Then
        mockMvc.perform(post("/lists")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listCreate)))
                .andExpect(status().isNotFound());
    }
}