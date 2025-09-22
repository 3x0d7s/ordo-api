package com.kyut.ordo.feature.card;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.card.dto.CardCreate;
import com.kyut.ordo.feature.card.dto.CardRead;
import com.kyut.ordo.feature.card.dto.CardWithItsListRead;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.exception.CardNotFoundException;
import com.kyut.ordo.feature.card.exception.InsufficientCardPermissionsException;
import com.kyut.ordo.feature.card.mapper.CardMapper;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.card.service.CardService;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.exception.ListNotFoundException;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * Unit tests for CardService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private ListRepository listRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardPermissionService boardPermissionService;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CardService cardService;

    private UserEntity testUser;
    private UserEntity assignedUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private CardCreate cardCreateDto;
    private CardRead cardReadDto;
    private CardWithItsListRead cardWithListReadDto;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        assignedUser = TestConfig.TestDataFactory.createTestUserWithId(2L, "assigned@example.com", "Assigned User");

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
        testCard.setDueDate(LocalDate.now().plusDays(7));
        testCard.setPosition(0);
        testCard.setList(testList);
        testCard.setCreatedBy(testUser);
        testCard.setAssignedTo(assignedUser);
        testCard.setCreatedAt(LocalDateTime.now());

        cardCreateDto = new CardCreate();
        cardCreateDto.setTitle("Test Card");
        cardCreateDto.setDescription("Test Description");
        cardCreateDto.setDueDate(LocalDate.now().plusDays(7));
        cardCreateDto.setPosition(0);
        cardCreateDto.setListId(1L);
        cardCreateDto.setAssignedToId(2L);

        cardReadDto = new CardRead();
        cardReadDto.setId(1L);
        cardReadDto.setTitle("Test Card");
        cardReadDto.setDescription("Test Description");

        cardWithListReadDto = new CardWithItsListRead();
        cardWithListReadDto.setId(1L);
        cardWithListReadDto.setTitle("Test Card");
        cardWithListReadDto.setDescription("Test Description");
    }

    @Test
    @DisplayName("Find all cards by list - success")
    void findAllByList_Success() throws ListNotFoundException, InsufficientBoardPermissionsException {
        // Given
        List<CardEntity> cards = List.of(testCard);
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(cardRepository.findAllByListOrderByPosition(testList)).thenReturn(cards);
        when(cardMapper.toDto(testCard)).thenReturn(cardReadDto);

        // When
        List<CardRead> result = cardService.findAllByList(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Card");
        verify(listRepository).findById(1L);
        verify(cardRepository).findAllByListOrderByPosition(testList);
        verify(cardMapper).toDto(testCard);
    }

    @Test
    @DisplayName("Find all cards by list - list not found")
    void findAllByList_ListNotFound() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.findAllByList(testUser, 1L))
            .isInstanceOf(ListNotFoundException.class)
            .hasMessageContaining("Task list not found with id: 1");
    }

    @Test
    @DisplayName("Find all cards by list with pagination - success")
    void findAllByListWithPagination_Success() throws ListNotFoundException, InsufficientBoardPermissionsException {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CardEntity> cards = List.of(testCard);
        Page<CardEntity> cardPage = new PageImpl<>(cards, pageable, 1);
        
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(cardRepository.findAllByList(testList, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(testCard)).thenReturn(cardReadDto);

        // When
        Page<CardRead> result = cardService.findAllByList(testUser, 1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Card");
        verify(listRepository).findById(1L);
        verify(cardRepository).findAllByList(testList, pageable);
    }

    @Test
    @DisplayName("Find all cards assigned to user - success")
    void findAllAssignedToUser_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CardEntity> cards = List.of(testCard);
        Page<CardEntity> cardPage = new PageImpl<>(cards, pageable, 1);
        
        when(cardRepository.findAllByAssignedTo(testUser, pageable)).thenReturn(cardPage);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        Page<CardWithItsListRead> result = cardService.findAllAssignedToUser(testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Card");
        verify(cardRepository).findAllByAssignedTo(testUser, pageable);
    }

    @Test
    @DisplayName("Find card by ID - success")
    void findById_Success() throws CardNotFoundException, InsufficientCardPermissionsException {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        CardWithItsListRead result = cardService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Card");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(cardMapper).toDtoWithItsList(testCard);
    }

    @Test
    @DisplayName("Find card by ID - card not found")
    void findById_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.findById(testUser, 1L))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Find card by ID - insufficient permissions")
    void findById_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cardService.findById(testUser, 1L))
            .isInstanceOf(InsufficientCardPermissionsException.class)
            .hasMessageContaining("User does not have permission to view this task");
    }

    @Test
    @DisplayName("Create card - success")
    void createCard_Success() throws ListNotFoundException, InsufficientCardPermissionsException {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedUser));
        when(cardMapper.toEntity(cardCreateDto, testList, testUser, assignedUser)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        CardWithItsListRead result = cardService.createCard(testUser, cardCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Card");
        verify(listRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "CREATE_TASKS");
        verify(cardRepository).save(testCard);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Create card - list not found")
    void createCard_ListNotFound() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(testUser, cardCreateDto))
            .isInstanceOf(ListNotFoundException.class)
            .hasMessageContaining("Task list not found with id: 1");
    }

    @Test
    @DisplayName("Create card - insufficient permissions")
    void createCard_InsufficientPermissions() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(testUser, cardCreateDto))
            .isInstanceOf(InsufficientCardPermissionsException.class)
            .hasMessageContaining("User does not have permission to create tasks in this list");
    }

    @Test
    @DisplayName("Create card - assigned user not found")
    void createCard_AssignedUserNotFound() {
        // Given
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(testUser, cardCreateDto))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("User not found with id: 2");
    }

    @Test
    @DisplayName("Create card without assigned user - success")
    void createCard_WithoutAssignedUser_Success() throws ListNotFoundException, InsufficientCardPermissionsException {
        // Given
        cardCreateDto.setAssignedToId(null);
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(true);
        when(cardMapper.toEntity(cardCreateDto, testList, testUser, null)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        CardWithItsListRead result = cardService.createCard(testUser, cardCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, never()).findById(anyLong());
        verify(cardMapper).toEntity(cardCreateDto, testList, testUser, null);
    }

    @Test
    @DisplayName("Update card - success")
    void updateCard_Success() throws CardNotFoundException, InsufficientCardPermissionsException {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        CardWithItsListRead result = cardService.updateCard(testUser, 1L, cardCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(cardMapper).updateEntityFromDto(cardCreateDto, testCard);
        verify(cardRepository).save(testCard);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Update card - card not found")
    void updateCard_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.updateCard(testUser, 1L, cardCreateDto))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Update card - insufficient permissions")
    void updateCard_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cardService.updateCard(testUser, 1L, cardCreateDto))
            .isInstanceOf(InsufficientCardPermissionsException.class)
            .hasMessageContaining("User does not have permission to edit this card");
    }

    @Test
    @DisplayName("Update card positions - success")
    void updateCardPositions_Success() throws ListNotFoundException, InsufficientBoardPermissionsException {
        // Given
        List<Long> cardIds = List.of(1L, 2L);
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        CardEntity secondCard = new CardEntity();
        secondCard.setId(2L);
        secondCard.setList(testList);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(secondCard));

        // When
        cardService.updateCardPositions(testUser, 1L, cardIds);

        // Then
        verify(listRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(cardRepository).save(testCard);
        verify(cardRepository).save(secondCard);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Update card positions - list not found")
    void updateCardPositions_ListNotFound() {
        // Given
        List<Long> cardIds = List.of(1L);
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.updateCardPositions(testUser, 1L, cardIds))
            .isInstanceOf(ListNotFoundException.class)
            .hasMessageContaining("List not found with id: 1");
    }

    @Test
    @DisplayName("Delete card - success")
    void deleteCard_Success() throws CardNotFoundException, InsufficientCardPermissionsException {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(cardMapper.toDtoWithItsList(testCard)).thenReturn(cardWithListReadDto);

        // When
        CardWithItsListRead result = cardService.deleteCard(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(cardRepository).deleteCommentsByCardId(1L);
        verify(cardRepository).deleteTasksByCardId(1L);
        verify(cardRepository).deleteById(1L);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Delete card - card not found")
    void deleteCard_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.deleteCard(testUser, 1L))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Delete card - insufficient permissions")
    void deleteCard_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> cardService.deleteCard(testUser, 1L))
            .isInstanceOf(InsufficientCardPermissionsException.class)
            .hasMessageContaining("User does not have permission to delete this task");
    }
}
