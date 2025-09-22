package com.kyut.ordo.feature.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.task.dto.TaskCreate;
import com.kyut.ordo.feature.task.entity.TaskEntity;
import com.kyut.ordo.feature.task.repository.TaskRepository;
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
 * Integration tests for TaskController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("TaskController Integration Tests with PostgreSQL")
@Transactional
class TaskControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TaskRepository taskRepository;

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
    private TaskEntity testTask;
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

        // Create test task
        testTask = createTestTask();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create task - success scenario")
    void createTask_Success() throws Exception {
        // Given
        TaskCreate taskCreate = new TaskCreate();
        taskCreate.setTitle("New Task");
        taskCreate.setDescription("New Task Description");
        taskCreate.setPosition(1);
        taskCreate.setCompleted(false);
        taskCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/tasks")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Task Description"))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.card.id").value(testCard.getId()));

        // Verify task was created in database
        List<TaskEntity> tasks = (List<TaskEntity>) taskRepository.findAll();
        assertThat(tasks).hasSize(2); // testTask + new task
        assertThat(tasks).anyMatch(task -> "New Task".equals(task.getTitle()));
    }

    @Test
    @DisplayName("Create task - unauthorized user")
    void createTask_Unauthorized() throws Exception {
        // Given
        TaskCreate taskCreate = new TaskCreate();
        taskCreate.setTitle("Unauthorized Task");
        taskCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreate)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create task - insufficient permissions")
    void createTask_InsufficientPermissions() throws Exception {
        // Given
        TaskCreate taskCreate = new TaskCreate();
        taskCreate.setTitle("Forbidden Task");
        taskCreate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(post("/tasks")
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create task - card not found")
    void createTask_CardNotFound() throws Exception {
        // Given
        TaskCreate taskCreate = new TaskCreate();
        taskCreate.setTitle("Task for non-existent card");
        taskCreate.setCardId(999L);

        // When & Then
        mockMvc.perform(post("/tasks")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get task by ID - success")
    void getTaskById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/tasks/{taskId}", testTask.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Task Description"))
                .andExpect(jsonPath("$.position").value(0))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("Get task by ID - not found")
    void getTaskById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/tasks/{taskId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get task by ID - insufficient permissions")
    void getTaskById_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(get("/tasks/{taskId}", testTask.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update task - success")
    void updateTask_Success() throws Exception {
        // Given
        TaskCreate taskUpdate = new TaskCreate();
        taskUpdate.setTitle("Updated Task");
        taskUpdate.setDescription("Updated Task Description");
        taskUpdate.setPosition(1);
        taskUpdate.setCompleted(true);
        taskUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/tasks/{taskId}", testTask.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Task Description"))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.completed").value(true));

        // Verify task was updated in database
        Optional<TaskEntity> updatedTask = taskRepository.findById(testTask.getId());
        assertThat(updatedTask).isPresent();
        assertThat(updatedTask.get().getTitle()).isEqualTo("Updated Task");
        assertThat(updatedTask.get().getCompleted()).isTrue();
    }

    @Test
    @DisplayName("Update task - not found")
    void updateTask_NotFound() throws Exception {
        // Given
        TaskCreate taskUpdate = new TaskCreate();
        taskUpdate.setTitle("Updated Task");
        taskUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/tasks/{taskId}", 999L)
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update task - insufficient permissions")
    void updateTask_InsufficientPermissions() throws Exception {
        // Given
        TaskCreate taskUpdate = new TaskCreate();
        taskUpdate.setTitle("Unauthorized Update");
        taskUpdate.setCardId(testCard.getId());

        // When & Then
        mockMvc.perform(put("/tasks/{taskId}", testTask.getId())
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete task - success")
    void deleteTask_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/tasks/{taskId}", testTask.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()));

        // Verify task is deleted
        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete task - not found")
    void deleteTask_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/tasks/{taskId}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete task - insufficient permissions")
    void deleteTask_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(delete("/tasks/{taskId}", testTask.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());

        // Verify task still exists
        assertThat(taskRepository.findById(testTask.getId())).isPresent();
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

    private TaskEntity createTestTask() {
        TaskEntity task = new TaskEntity();
        task.setTitle("Test Task");
        task.setDescription("Test Task Description");
        task.setPosition(0);
        task.setCompleted(false);
        task.setCard(testCard);
        return taskRepository.save(task);
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
