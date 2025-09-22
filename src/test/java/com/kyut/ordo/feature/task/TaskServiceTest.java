package com.kyut.ordo.feature.task;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.exception.CardNotFoundException;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.task.dto.TaskCreate;
import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.dto.TaskWithItsCardRead;
import com.kyut.ordo.feature.task.entity.TaskEntity;
import com.kyut.ordo.feature.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.feature.task.exception.TaskNotFoundException;
import com.kyut.ordo.feature.task.mapper.TaskMapper;
import com.kyut.ordo.feature.task.repository.TaskRepository;
import com.kyut.ordo.feature.task.service.TaskService;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private BoardPermissionService boardPermissionService;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private UserEntity testUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private TaskEntity testTask;
    private TaskCreate taskCreateDto;
    private TaskRead taskReadDto;
    private TaskWithItsCardRead taskWithCardReadDto;

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

        testList = new ListEntity();
        testList.setId(1L);
        testList.setTitle("Test List");
        testList.setPosition(0);
        testList.setBoard(testBoard);
        testList.setCreatedAt(LocalDateTime.now());

        testCard = new CardEntity();
        testCard.setId(1L);
        testCard.setTitle("Test Card");
        testCard.setDescription("Test Description");
        testCard.setPosition(0);
        testCard.setList(testList);
        testCard.setCreatedBy(testUser);
        testCard.setCreatedAt(LocalDateTime.now());

        testTask = new TaskEntity();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Task Description");
        testTask.setPosition(0);
        testTask.setCompleted(false);
        testTask.setCard(testCard);

        taskCreateDto = new TaskCreate();
        taskCreateDto.setTitle("Test Task");
        taskCreateDto.setDescription("Test Task Description");
        taskCreateDto.setPosition(0);
        taskCreateDto.setCompleted(false);
        taskCreateDto.setCardId(1L);

        taskReadDto = new TaskRead();
        taskReadDto.setId(1L);
        taskReadDto.setTitle("Test Task");
        taskReadDto.setDescription("Test Task Description");
        taskReadDto.setPosition(0);
        taskReadDto.setCompleted(false);

        taskWithCardReadDto = new TaskWithItsCardRead();
        taskWithCardReadDto.setId(1L);
        taskWithCardReadDto.setTitle("Test Task");
        taskWithCardReadDto.setDescription("Test Task Description");
        taskWithCardReadDto.setPosition(0);
        taskWithCardReadDto.setCompleted(false);
    }

    @Test
    @DisplayName("Find all tasks by card - success")
    void findAllByCard_Success() throws CardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        List<TaskEntity> tasks = List.of(testTask);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskRepository.findAllByCardOrderByPosition(testCard)).thenReturn(tasks);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        List<TaskRead> result = taskService.findAllByCard(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskRepository).findAllByCardOrderByPosition(testCard);
        verify(taskMapper).toDto(testTask);
    }

    @Test
    @DisplayName("Find all tasks by card - card not found")
    void findAllByCard_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.findAllByCard(testUser, 1L))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Card not found with id: 1");
    }

    @Test
    @DisplayName("Find all tasks by card - insufficient permissions")
    void findAllByCard_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.findAllByCard(testUser, 1L))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to view tasks in this card");
    }

    @Test
    @DisplayName("Find all tasks by card with pagination - success")
    void findAllByCardWithPagination_Success() throws CardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = List.of(testTask);
        Page<TaskEntity> taskPage = new PageImpl<>(tasks, pageable, 1);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskRepository.findAllByCard(testCard, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        Page<TaskRead> result = taskService.findAllByCard(testUser, 1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Task");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskRepository).findAllByCard(testCard, pageable);
    }

    @Test
    @DisplayName("Find task by ID - success")
    void findById_Success() throws TaskNotFoundException, InsufficientTaskPermissionsException {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        TaskRead result = taskService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskMapper).toDto(testTask);
    }

    @Test
    @DisplayName("Find task by ID - task not found")
    void findById_TaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.findById(testUser, 1L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Find task by ID - insufficient permissions")
    void findById_InsufficientPermissions() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.findById(testUser, 1L))
            .isInstanceOf(InsufficientTaskPermissionsException.class)
            .hasMessageContaining("User does not have permission to view this task");
    }

    @Test
    @DisplayName("Create task - success")
    void createTask_Success() throws CardNotFoundException, InsufficientTaskPermissionsException {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskMapper.toEntity(taskCreateDto, testCard)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);
        when(taskMapper.toDtoWithItsCard(testTask)).thenReturn(taskWithCardReadDto);

        // When
        TaskWithItsCardRead result = taskService.createTask(testUser, taskCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskRepository).save(testTask);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Create task - card not found")
    void createTask_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testUser, taskCreateDto))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Card not found with id: 1");
    }

    @Test
    @DisplayName("Create task - insufficient permissions")
    void createTask_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testUser, taskCreateDto))
            .isInstanceOf(InsufficientTaskPermissionsException.class)
            .hasMessageContaining("User does not have permission to create tasks in this card");
    }

    @Test
    @DisplayName("Create task with auto position - success")
    void createTask_WithAutoPosition_Success() throws CardNotFoundException, InsufficientTaskPermissionsException {
        // Given
        taskCreateDto.setPosition(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskRepository.countByCard(testCard)).thenReturn(2);
        when(taskMapper.toEntity(taskCreateDto, testCard)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);
        when(taskMapper.toDtoWithItsCard(testTask)).thenReturn(taskWithCardReadDto);

        // When
        TaskWithItsCardRead result = taskService.createTask(testUser, taskCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(taskCreateDto.getPosition()).isEqualTo(2);
        verify(taskRepository).countByCard(testCard);
    }

    @Test
    @DisplayName("Create task with auto completed flag - success")
    void createTask_WithAutoCompleted_Success() throws CardNotFoundException, InsufficientTaskPermissionsException {
        // Given
        taskCreateDto.setCompleted(null);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskMapper.toEntity(taskCreateDto, testCard)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);
        when(taskMapper.toDtoWithItsCard(testTask)).thenReturn(taskWithCardReadDto);

        // When
        TaskWithItsCardRead result = taskService.createTask(testUser, taskCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(taskCreateDto.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("Update task - success")
    void updateTask_Success() throws TaskNotFoundException, InsufficientTaskPermissionsException, CardNotFoundException {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        TaskRead result = taskService.updateTask(testUser, 1L, taskCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskMapper).updateEntityFromDto(taskCreateDto, testTask);
        verify(taskRepository).save(testTask);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Update task - task not found")
    void updateTask_TaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(testUser, 1L, taskCreateDto))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Update task - insufficient permissions")
    void updateTask_InsufficientPermissions() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(testUser, 1L, taskCreateDto))
            .isInstanceOf(InsufficientTaskPermissionsException.class)
            .hasMessageContaining("User does not have permission to edit this task");
    }

    @Test
    @DisplayName("Update task with card change - success")
    void updateTask_WithCardChange_Success() throws TaskNotFoundException, InsufficientTaskPermissionsException, CardNotFoundException {
        // Given
        CardEntity newCard = new CardEntity();
        newCard.setId(2L);
        newCard.setList(testList); // Same board
        
        taskCreateDto.setCardId(2L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(newCard));
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        TaskRead result = taskService.updateTask(testUser, 1L, taskCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(cardRepository).findById(2L);
        // Verify that the task's card was updated (we can't verify setCard on real entity)
        assertThat(testTask.getCard()).isEqualTo(newCard);
    }

    @Test
    @DisplayName("Update task with card change to different board - error")
    void updateTask_WithCardChangeToDifferentBoard_Error() {
        // Given
        BoardEntity differentBoard = new BoardEntity();
        differentBoard.setId(2L);
        
        ListEntity differentList = new ListEntity();
        differentList.setBoard(differentBoard);
        
        CardEntity newCard = new CardEntity();
        newCard.setId(2L);
        newCard.setList(differentList);
        
        taskCreateDto.setCardId(2L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(newCard));

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(testUser, 1L, taskCreateDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot move task to a card in a different board");
    }

    @Test
    @DisplayName("Update task with new card not found - error")
    void updateTask_WithNewCardNotFound_Error() {
        // Given
        taskCreateDto.setCardId(2L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(testUser, 1L, taskCreateDto))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Card not found with id: 2");
    }

    @Test
    @DisplayName("Delete task - success")
    void deleteTask_Success() throws TaskNotFoundException, InsufficientTaskPermissionsException {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(taskMapper.toDto(testTask)).thenReturn(taskReadDto);

        // When
        TaskRead result = taskService.deleteTask(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(taskRepository).delete(testTask);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Delete task - task not found")
    void deleteTask_TaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(testUser, 1L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Delete task - insufficient permissions")
    void deleteTask_InsufficientPermissions() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(testUser, 1L))
            .isInstanceOf(InsufficientTaskPermissionsException.class)
            .hasMessageContaining("User does not have permission to delete this task");
    }
}
