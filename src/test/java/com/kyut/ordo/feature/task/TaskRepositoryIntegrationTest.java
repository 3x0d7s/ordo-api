package com.kyut.ordo.feature.task;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.task.entity.TaskEntity;
import com.kyut.ordo.feature.task.repository.TaskRepository;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import com.kyut.ordo.testcontainers.PostgreSQLTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository using PostgreSQL with Testcontainers.
 * Tests the data access layer and custom repository methods.
 */
@Import({TestConfig.class, PostgreSQLTestDataBuilder.class})
@DisplayName("TaskRepository Integration Tests with PostgreSQL")
@Transactional
class TaskRepositoryIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;
    
    @Autowired
    private CardRepository cardRepository;

    private UserEntity testUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private CardEntity secondCard;
    private TaskEntity firstTask;
    private TaskEntity secondTask;
    private TaskEntity thirdTask;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test user
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");

        // Create test workspace
        testWorkspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);

        // Create test board
        testBoard = dataBuilder.createTestBoardWithWorkspace("Test Board", "Test Description", testUser, testWorkspace);

        // Create test list
        testList = dataBuilder.createTestList("Test List", 0, testBoard);

        // Create test cards
        testCard = createTestCard("Test Card", 0);
        secondCard = createTestCard("Second Card", 1);

        // Create test tasks
        createTestTasks();
    }

    @Test
    @DisplayName("Find all tasks by card ordered by position - success")
    void findAllByCardOrderByPosition_Success() {
        // When
        List<TaskEntity> result = taskRepository.findAllByCardOrderByPosition(testCard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPosition()).isEqualTo(0);
        assertThat(result.get(0).getTitle()).isEqualTo("First Task");
        assertThat(result.get(1).getPosition()).isEqualTo(1);
        assertThat(result.get(1).getTitle()).isEqualTo("Second Task");
    }

    @Test
    @DisplayName("Find all tasks by card ordered by position - empty result")
    void findAllByCardOrderByPosition_EmptyResult() {
        // Given
        CardEntity emptyCard = createTestCard("Empty Card", 2);

        // When
        List<TaskEntity> result = taskRepository.findAllByCardOrderByPosition(emptyCard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find all tasks by card with pagination - success")
    void findAllByCardWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<TaskEntity> result = taskRepository.findAllByCard(testCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Find all tasks by card with pagination - second page")
    void findAllByCardWithPagination_SecondPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);

        // When
        Page<TaskEntity> result = taskRepository.findAllByCard(testCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Find all tasks by card with pagination - empty result")
    void findAllByCardWithPagination_EmptyResult() {
        // Given
        CardEntity emptyCard = createTestCard("Empty Card", 2);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TaskEntity> result = taskRepository.findAllByCard(emptyCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Count tasks by card - success")
    void countByCard_Success() {
        // When
        Integer count = taskRepository.countByCard(testCard);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Count tasks by card - zero count")
    void countByCard_ZeroCount() {
        // Given
        CardEntity emptyCard = createTestCard("Empty Card", 2);

        // When
        Integer count = taskRepository.countByCard(emptyCard);

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Save task - success")
    void saveTask_Success() {
        // Given
        TaskEntity newTask = new TaskEntity();
        newTask.setTitle("New Task");
        newTask.setDescription("New Task Description");
        newTask.setPosition(2);
        newTask.setCompleted(false);
        newTask.setCard(testCard);

        // When
        TaskEntity savedTask = taskRepository.save(newTask);

        // Then
        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("New Task");
        assertThat(savedTask.getDescription()).isEqualTo("New Task Description");
        assertThat(savedTask.getPosition()).isEqualTo(2);
        assertThat(savedTask.getCompleted()).isFalse();
        assertThat(savedTask.getCard()).isEqualTo(testCard);
    }

    @Test
    @DisplayName("Update task - success")
    void updateTask_Success() {
        // Given
        firstTask.setTitle("Updated Task Title");
        firstTask.setDescription("Updated Task Description");
        firstTask.setCompleted(true);
        firstTask.setPosition(5);

        // When
        TaskEntity updatedTask = taskRepository.save(firstTask);

        // Then
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getId()).isEqualTo(firstTask.getId());
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Task Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated Task Description");
        assertThat(updatedTask.getCompleted()).isTrue();
        assertThat(updatedTask.getPosition()).isEqualTo(5);
    }

    @Test
    @DisplayName("Delete task - success")
    void deleteTask_Success() {
        // Given
        Long taskId = firstTask.getId();

        // When
        taskRepository.delete(firstTask);

        // Then
        assertThat(taskRepository.findById(taskId)).isEmpty();
        
        // Verify other tasks still exist
        List<TaskEntity> remainingTasks = taskRepository.findAllByCardOrderByPosition(testCard);
        assertThat(remainingTasks).hasSize(1);
        assertThat(remainingTasks.get(0).getTitle()).isEqualTo("Second Task");
    }

    @Test
    @DisplayName("Find task by ID - success")
    void findById_Success() {
        // When
        var result = taskRepository.findById(firstTask.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("First Task");
        assertThat(result.get().getCard()).isEqualTo(testCard);
    }

    @Test
    @DisplayName("Find task by ID - not found")
    void findById_NotFound() {
        // When
        var result = taskRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Tasks ordering is preserved across different cards")
    void tasksOrderingPreservedAcrossDifferentCards() {
        // When
        List<TaskEntity> firstCardTasks = taskRepository.findAllByCardOrderByPosition(testCard);
        List<TaskEntity> secondCardTasks = taskRepository.findAllByCardOrderByPosition(secondCard);

        // Then
        assertThat(firstCardTasks).hasSize(2);
        assertThat(firstCardTasks.get(0).getPosition()).isEqualTo(0);
        assertThat(firstCardTasks.get(1).getPosition()).isEqualTo(1);

        assertThat(secondCardTasks).hasSize(1);
        assertThat(secondCardTasks.get(0).getPosition()).isEqualTo(0);
        assertThat(secondCardTasks.get(0).getTitle()).isEqualTo("Third Task");
    }

    private CardEntity createTestCard(String title, int position) {
        CardEntity card = new CardEntity();
        card.setTitle(title);
        card.setDescription(title + " Description");
        card.setPosition(position);
        card.setList(testList);
        card.setCreatedBy(testUser);
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    private void createTestTasks() {
        // First task for testCard
        firstTask = new TaskEntity();
        firstTask.setTitle("First Task");
        firstTask.setDescription("First Task Description");
        firstTask.setPosition(0);
        firstTask.setCompleted(false);
        firstTask.setCard(testCard);
        firstTask = taskRepository.save(firstTask);

        // Second task for testCard
        secondTask = new TaskEntity();
        secondTask.setTitle("Second Task");
        secondTask.setDescription("Second Task Description");
        secondTask.setPosition(1);
        secondTask.setCompleted(true);
        secondTask.setCard(testCard);
        secondTask = taskRepository.save(secondTask);

        // Third task for secondCard
        thirdTask = new TaskEntity();
        thirdTask.setTitle("Third Task");
        thirdTask.setDescription("Third Task Description");
        thirdTask.setPosition(0);
        thirdTask.setCompleted(false);
        thirdTask.setCard(secondCard);
        thirdTask = taskRepository.save(thirdTask);

    }
}
