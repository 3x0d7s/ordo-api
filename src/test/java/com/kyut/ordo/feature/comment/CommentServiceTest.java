package com.kyut.ordo.feature.comment;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.exception.CardNotFoundException;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.comment.dto.CommentCreate;
import com.kyut.ordo.feature.comment.dto.CommentRead;
import com.kyut.ordo.feature.comment.entity.CommentEntity;
import com.kyut.ordo.feature.comment.exception.CommentNotFoundException;
import com.kyut.ordo.feature.comment.exception.InsufficientCommentPermissionsException;
import com.kyut.ordo.feature.comment.mapper.CommentMapper;
import com.kyut.ordo.feature.comment.repository.CommentRepository;
import com.kyut.ordo.feature.comment.service.CommentService;
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
 * Unit tests for CommentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Unit Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private BoardPermissionService boardPermissionService;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    private UserEntity testUser;
    private UserEntity commentCreator;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private CommentEntity testComment;
    private CommentCreate commentCreateDto;
    private CommentRead commentReadDto;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        commentCreator = TestConfig.TestDataFactory.createTestUserWithId(2L, "creator@example.com", "Comment Creator");

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

        testComment = new CommentEntity();
        testComment.setId(1L);
        testComment.setMessage("Test Comment Message");
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setCreatedBy(commentCreator);
        testComment.setCard(testCard);

        commentCreateDto = new CommentCreate();
        commentCreateDto.setMessage("Test Comment Message");
        commentCreateDto.setCardId(1L);

        commentReadDto = new CommentRead();
        commentReadDto.setId(1L);
        commentReadDto.setMessage("Test Comment Message");
        commentReadDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Find all comments by card - success")
    void findAllByCard_Success() throws CardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        List<CommentEntity> comments = List.of(testComment);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(commentRepository.findAllByCardOrderByCreatedAtDesc(testCard)).thenReturn(comments);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        List<CommentRead> result = commentService.findAllByCard(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Test Comment Message");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(commentRepository).findAllByCardOrderByCreatedAtDesc(testCard);
        verify(commentMapper).toDto(testComment);
    }

    @Test
    @DisplayName("Find all comments by card - card not found")
    void findAllByCard_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.findAllByCard(testUser, 1L))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Find all comments by card - insufficient permissions")
    void findAllByCard_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentService.findAllByCard(testUser, 1L))
            .isInstanceOf(InsufficientBoardPermissionsException.class)
            .hasMessageContaining("User does not have permission to view comments in this task");
    }

    @Test
    @DisplayName("Find all comments by card with pagination - success")
    void findAllByCardWithPagination_Success() throws CardNotFoundException, InsufficientBoardPermissionsException {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentEntity> comments = List.of(testComment);
        Page<CommentEntity> commentPage = new PageImpl<>(comments, pageable, 1);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(commentRepository.findAllByCard(testCard, pageable)).thenReturn(commentPage);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        Page<CommentRead> result = commentService.findAllByCard(testUser, 1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMessage()).isEqualTo("Test Comment Message");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(commentRepository).findAllByCard(testCard, pageable);
    }

    @Test
    @DisplayName("Find all comments by user - success")
    void findAllByUser_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentEntity> comments = List.of(testComment);
        Page<CommentEntity> commentPage = new PageImpl<>(comments, pageable, 1);
        
        when(commentRepository.findAllByCreatedBy(testUser, pageable)).thenReturn(commentPage);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        Page<CommentRead> result = commentService.findAllByUser(testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMessage()).isEqualTo("Test Comment Message");
        verify(commentRepository).findAllByCreatedBy(testUser, pageable);
    }

    @Test
    @DisplayName("Find comment by ID - success")
    void findById_Success() throws CommentNotFoundException, InsufficientCommentPermissionsException {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(true);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Test Comment Message");
        verify(commentRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "EDIT");
        verify(commentMapper).toDto(testComment);
    }

    @Test
    @DisplayName("Find comment by ID - comment not found")
    void findById_CommentNotFound() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.findById(testUser, 1L))
            .isInstanceOf(CommentNotFoundException.class)
            .hasMessageContaining("Comment not found with id: 1");
    }

    @Test
    @DisplayName("Find comment by ID - insufficient permissions")
    void findById_InsufficientPermissions() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentService.findById(testUser, 1L))
            .isInstanceOf(InsufficientCommentPermissionsException.class)
            .hasMessageContaining("User does not have permission to view this comment");
    }

    @Test
    @DisplayName("Create comment - success")
    void createComment_Success() throws CardNotFoundException, InsufficientCommentPermissionsException {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(true);
        when(commentMapper.toEntity(commentCreateDto, testUser, testCard)).thenReturn(testComment);
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.createComment(testUser, commentCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Test Comment Message");
        verify(cardRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "CREATE_TASKS");
        verify(commentRepository).save(testComment);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Create comment - card not found")
    void createComment_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testUser, commentCreateDto))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    @DisplayName("Create comment - insufficient permissions")
    void createComment_InsufficientPermissions() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testUser, commentCreateDto))
            .isInstanceOf(InsufficientCommentPermissionsException.class)
            .hasMessageContaining("User does not have permission to create comments in this task");
    }

    @Test
    @DisplayName("Update comment - success by comment creator")
    void updateComment_SuccessByCreator() throws CommentNotFoundException, InsufficientCommentPermissionsException {
        // Given
        testComment.setCreatedBy(testUser); // User is the creator
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.updateComment(testUser, 1L, commentCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(1L);
        verify(commentRepository).save(testComment);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Update comment - success by board admin")
    void updateComment_SuccessByBoardAdmin() throws CommentNotFoundException, InsufficientCommentPermissionsException {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).thenReturn(true);
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.updateComment(testUser, 1L, commentCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "MANAGE_ROLES");
        verify(commentRepository).save(testComment);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Update comment - comment not found")
    void updateComment_CommentNotFound() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(testUser, 1L, commentCreateDto))
            .isInstanceOf(CommentNotFoundException.class)
            .hasMessageContaining("Comment not found with id: 1");
    }

    @Test
    @DisplayName("Update comment - insufficient permissions")
    void updateComment_InsufficientPermissions() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(testUser, 1L, commentCreateDto))
            .isInstanceOf(InsufficientCommentPermissionsException.class)
            .hasMessageContaining("User does not have permission to edit this comment");
    }

    @Test
    @DisplayName("Update comment with different card ID - error")
    void updateComment_WithDifferentCardId_Error() {
        // Given
        testComment.setCreatedBy(testUser);
        commentCreateDto.setCardId(2L); // Different card ID
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(testUser, 1L, commentCreateDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot change the task of a comment");
    }

    @Test
    @DisplayName("Delete comment - success by comment creator")
    void deleteComment_SuccessByCreator() throws CommentNotFoundException, InsufficientCommentPermissionsException {
        // Given
        testComment.setCreatedBy(testUser); // User is the creator
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.deleteComment(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(1L);
        verify(commentRepository).delete(testComment);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Delete comment - success by board admin")
    void deleteComment_SuccessByBoardAdmin() throws CommentNotFoundException, InsufficientCommentPermissionsException {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).thenReturn(true);
        when(commentMapper.toDto(testComment)).thenReturn(commentReadDto);

        // When
        CommentRead result = commentService.deleteComment(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(1L);
        verify(boardPermissionService).hasPermission(1L, testUser.getId(), "MANAGE_ROLES");
        verify(commentRepository).delete(testComment);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Delete comment - comment not found")
    void deleteComment_CommentNotFound() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(testUser, 1L))
            .isInstanceOf(CommentNotFoundException.class)
            .hasMessageContaining("Comment not found with id: 1");
    }

    @Test
    @DisplayName("Delete comment - insufficient permissions")
    void deleteComment_InsufficientPermissions() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(testUser, 1L))
            .isInstanceOf(InsufficientCommentPermissionsException.class)
            .hasMessageContaining("User does not have permission to delete this comment");
    }
}
