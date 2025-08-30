package com.kyut.ordo.list.service;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.repository.BoardRepository;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.list.dto.ListCreate;
import com.kyut.ordo.list.dto.ListRead;
import com.kyut.ordo.list.entity.ListEntity;
import com.kyut.ordo.list.event.ListCreatedEvent;
import com.kyut.ordo.list.exception.ListNotFoundException;
import com.kyut.ordo.list.mapper.ListMapper;
import com.kyut.ordo.list.repository.ListRepository;
import com.kyut.ordo.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit тести для ListService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListService Unit Tests")
class ListServiceTest {

    @Mock
    private ListRepository listRepository;
    
    @Mock
    private BoardRepository boardRepository;
    
    @Mock
    private BoardPermissionService boardPermissionService;
    
    @Mock
    private ListMapper listMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private ListService listService;
    
    private UserEntity testUser;
    private BoardEntity testBoard;
    private ListEntity testList;
    private ListCreate listCreateDto;
    private ListRead listReadDto;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        
        testBoard = new BoardEntity();
        testBoard.setId(1L);
        testBoard.setTitle("Test Board");
        
        testList = new ListEntity();
        testList.setId(1L);
        testList.setTitle("Test List");
        testList.setPosition(0);
        testList.setBoard(testBoard);
        
        listCreateDto = new ListCreate();
        listCreateDto.setBoardId(1L);
        listCreateDto.setTitle("Test List");
        listCreateDto.setPosition(0);
        
        listReadDto = new ListRead();
        listReadDto.setId(1L);
        listReadDto.setTitle("Test List");
        listReadDto.setPosition(0);
    }

    @Test
    @DisplayName("Створення списку - успішний сценарій")
    void createTaskList_Success() throws InsufficientBoardPermissionsException {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).thenReturn(true);
        when(listMapper.toEntity(listCreateDto, testBoard)).thenReturn(testList);
        when(listRepository.save(testList)).thenReturn(testList);
        when(listMapper.toDto(testList)).thenReturn(listReadDto);

        // When
        ListRead result = listService.createTaskList(testUser, listCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test List");
        
        // Перевіряємо що подія була опублікована
        ArgumentCaptor<ListCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ListCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        ListCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getListId()).isEqualTo(1L);
        assertThat(publishedEvent.getBoardId()).isEqualTo(1L);
        assertThat(publishedEvent.getListData()).isEqualTo(listReadDto);
        
        // Перевіряємо виклики
        verify(boardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "CREATE_LISTS");
        verify(listRepository).save(testList);
    }

    @Test
    @DisplayName("Створення списку - дошка не знайдена")
    void createTaskList_BoardNotFound() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> listService.createTaskList(testUser, listCreateDto))
            .isInstanceOf(BoardNotFoundException.class)
            .hasMessageContaining("Board not found with id: 1");
        
        // Перевіряємо що подія НЕ була опублікована
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Створення списку - недостатньо прав")
    void createTaskList_InsufficientPermissions() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> listService.createTaskList(testUser, listCreateDto))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to create lists");
        
        // Перевіряємо що подія НЕ була опублікована
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Створення списку - автоматичне встановлення позиції")
    void createTaskList_AutoPosition() throws InsufficientBoardPermissionsException {
        // Given
        listCreateDto.setPosition(null); // Не вказуємо позицію
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).thenReturn(true);
        when(listRepository.countByBoard(testBoard)).thenReturn(3); // Вже є 3 списки
        when(listMapper.toEntity(listCreateDto, testBoard)).thenReturn(testList);
        when(listRepository.save(testList)).thenReturn(testList);
        when(listMapper.toDto(testList)).thenReturn(listReadDto);

        // When
        listService.createTaskList(testUser, listCreateDto);

        // Then
        assertThat(listCreateDto.getPosition()).isEqualTo(3); // Повинна встановитись позиція 3
        verify(listRepository).countByBoard(testBoard);
    }

    @Test
    @DisplayName("Отримання списку по ID - успішний сценарій")
    void findById_Success() throws InsufficientBoardPermissionsException {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(listMapper.toDto(testList)).thenReturn(listReadDto);

        // When
        ListRead result = listService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test List");
        
        verify(listRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
    }

    @Test
    @DisplayName("Отримання списку по ID - список не знайдено")
    void findById_ListNotFound() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> listService.findById(testUser, 1L))
            .isInstanceOf(ListNotFoundException.class)
            .hasMessageContaining("Task list not found with id: 1");
    }
}
