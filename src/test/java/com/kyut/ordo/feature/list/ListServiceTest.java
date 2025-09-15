package com.kyut.ordo.feature.list;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.list.dto.ListCreate;
import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.event.ListCreatedEvent;
import com.kyut.ordo.feature.list.exception.ListNotFoundException;
import com.kyut.ordo.feature.list.mapper.ListMapper;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.list.service.ListService;
import com.kyut.ordo.feature.user.entity.UserEntity;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ListService
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
    @DisplayName("Create list - success scenario")
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
        
        // Verify that the event was published
        ArgumentCaptor<ListCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ListCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        ListCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getListId()).isEqualTo(1L);
        assertThat(publishedEvent.getBoardId()).isEqualTo(1L);
        assertThat(publishedEvent.getListData()).isEqualTo(listReadDto);
        
        // Verify method calls
        verify(boardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "CREATE_LISTS");
        verify(listRepository).save(testList);
    }

    @Test
    @DisplayName("Create list - board not found")
    void createTaskList_BoardNotFound() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> listService.createTaskList(testUser, listCreateDto))
            .isInstanceOf(BoardNotFoundException.class)
            .hasMessageContaining("Board not found with id: 1");
        
        // Verify that the event was NOT published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Create list - insufficient permissions")
    void createTaskList_InsufficientPermissions() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> listService.createTaskList(testUser, listCreateDto))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to create lists");
        
        // Verify that the event was NOT published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Create list - automatic position setting")
    void createTaskList_AutoPosition() throws InsufficientBoardPermissionsException {
        // Given
        listCreateDto.setPosition(null); // Do not specify position
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).thenReturn(true);
        when(listRepository.countByBoard(testBoard)).thenReturn(3); // Already have 3 lists
        when(listMapper.toEntity(listCreateDto, testBoard)).thenReturn(testList);
        when(listRepository.save(testList)).thenReturn(testList);
        when(listMapper.toDto(testList)).thenReturn(listReadDto);

        // When
        listService.createTaskList(testUser, listCreateDto);

        // Then
        assertThat(listCreateDto.getPosition()).isEqualTo(3); // Should set position to 3
        verify(listRepository).countByBoard(testBoard);
    }

    @Test
    @DisplayName("Get list by ID - success scenario")
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
    @DisplayName("Get list by ID - list not found")
    void findById_ListNotFound() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> listService.findById(testUser, 1L))
            .isInstanceOf(ListNotFoundException.class)
            .hasMessageContaining("Task list not found with id: 1");
    }
}
