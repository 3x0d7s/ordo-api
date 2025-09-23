package com.kyut.ordo.feature.comment;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.repository.CardRepository;
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
 * Integration tests for CommentRepository using PostgreSQL with Testcontainers.
 * Tests the data access layer and custom repository methods.
 */
@Import({TestConfig.class, PostgreSQLTestDataBuilder.class})
@ActiveProfiles("testcontainers")
@DisplayName("CommentRepository Integration Tests with PostgreSQL")
@Transactional
class CommentRepositoryIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;
    
    @Autowired
    private CardRepository cardRepository;

    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private CardEntity testCard;
    private CardEntity secondCard;
    private CommentEntity firstComment;
    private CommentEntity secondComment;
    private CommentEntity thirdComment;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test users
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        otherUser = dataBuilder.createTestUser("other@example.com", "Other User");

        // Create test workspace
        testWorkspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);

        // Create test board
        testBoard = dataBuilder.createTestBoardWithWorkspace("Test Board", "Test Description", testUser, testWorkspace);

        // Create test list
        testList = dataBuilder.createTestList("Test List", 0, testBoard);

        // Create test cards
        testCard = createTestCard("Test Card", 0);
        secondCard = createTestCard("Second Card", 1);

        // Create test comments
        createTestComments();
    }

    @Test
    @DisplayName("Find all comments by card ordered by created date desc - success")
    void findAllByCardOrderByCreatedAtDesc_Success() {
        // When
        List<CommentEntity> result = commentRepository.findAllByCardOrderByCreatedAtDesc(testCard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        // Should be ordered by created date desc (most recent first)
        assertThat(result.get(0).getMessage()).isEqualTo("Second Comment");
        assertThat(result.get(1).getMessage()).isEqualTo("First Comment");
    }

    @Test
    @DisplayName("Find all comments by card ordered by created date desc - empty result")
    void findAllByCardOrderByCreatedAtDesc_EmptyResult() {
        // Given
        CardEntity emptyCard = createTestCard("Empty Card", 2);

        // When
        List<CommentEntity> result = commentRepository.findAllByCardOrderByCreatedAtDesc(emptyCard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find all comments by card with pagination - success")
    void findAllByCardWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCard(testCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Find all comments by card with pagination - second page")
    void findAllByCardWithPagination_SecondPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCard(testCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Find all comments by card with pagination - empty result")
    void findAllByCardWithPagination_EmptyResult() {
        // Given
        CardEntity emptyCard = createTestCard("Empty Card", 2);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCard(emptyCard, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Find all comments by user - success")
    void findAllByCreatedBy_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCreatedBy(testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // firstComment and secondComment
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(comment -> comment.getCreatedBy().equals(testUser));
    }

    @Test
    @DisplayName("Find all comments by user - different user")
    void findAllByCreatedBy_DifferentUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCreatedBy(otherUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1); // thirdComment
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMessage()).isEqualTo("Third Comment");
    }

    @Test
    @DisplayName("Find all comments by user - empty result")
    void findAllByCreatedBy_EmptyResult() {
        // Given
        UserEntity newUser = dataBuilder.createTestUser("new@example.com", "New User");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CommentEntity> result = commentRepository.findAllByCreatedBy(newUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Save comment - success")
    void saveComment_Success() {
        // Given
        CommentEntity newComment = new CommentEntity();
        newComment.setMessage("New Comment Message");
        newComment.setCreatedBy(testUser);
        newComment.setCard(testCard);
        newComment.setCreatedAt(LocalDateTime.now());

        // When
        CommentEntity savedComment = commentRepository.save(newComment);

        // Then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getMessage()).isEqualTo("New Comment Message");
        assertThat(savedComment.getCreatedBy()).isEqualTo(testUser);
        assertThat(savedComment.getCard()).isEqualTo(testCard);
        assertThat(savedComment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Update comment - success")
    void updateComment_Success() {
        // Given
        firstComment.setMessage("Updated Comment Message");

        // When
        CommentEntity updatedComment = commentRepository.save(firstComment);

        // Then
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getId()).isEqualTo(firstComment.getId());
        assertThat(updatedComment.getMessage()).isEqualTo("Updated Comment Message");
        assertThat(updatedComment.getCreatedBy()).isEqualTo(firstComment.getCreatedBy());
        assertThat(updatedComment.getCard()).isEqualTo(firstComment.getCard());
    }

    @Test
    @DisplayName("Delete comment - success")
    void deleteComment_Success() {
        // Given
        Long commentId = firstComment.getId();

        // When
        commentRepository.delete(firstComment);

        // Then
        assertThat(commentRepository.findById(commentId)).isEmpty();
        
        // Verify other comments still exist
        List<CommentEntity> remainingComments = commentRepository.findAllByCardOrderByCreatedAtDesc(testCard);
        assertThat(remainingComments).hasSize(1);
        assertThat(remainingComments.get(0).getMessage()).isEqualTo("Second Comment");
    }

    @Test
    @DisplayName("Find comment by ID - success")
    void findById_Success() {
        // When
        var result = commentRepository.findById(firstComment.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("First Comment");
        assertThat(result.get().getCard()).isEqualTo(testCard);
        assertThat(result.get().getCreatedBy()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Find comment by ID - not found")
    void findById_NotFound() {
        // When
        var result = commentRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Comments are properly associated with different cards")
    void commentsAssociatedWithDifferentCards() {
        // When
        List<CommentEntity> firstCardComments = commentRepository.findAllByCardOrderByCreatedAtDesc(testCard);
        List<CommentEntity> secondCardComments = commentRepository.findAllByCardOrderByCreatedAtDesc(secondCard);

        // Then
        assertThat(firstCardComments).hasSize(2);
        assertThat(firstCardComments).allMatch(comment -> comment.getCard().equals(testCard));

        assertThat(secondCardComments).hasSize(1);
        assertThat(secondCardComments.get(0).getCard()).isEqualTo(secondCard);
        assertThat(secondCardComments.get(0).getMessage()).isEqualTo("Third Comment");
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

    private void createTestComments() {
        // First comment for testCard (created by testUser) - older
        firstComment = new CommentEntity();
        firstComment.setMessage("First Comment");
        firstComment.setCreatedBy(testUser);
        firstComment.setCard(testCard);
        firstComment.setCreatedAt(LocalDateTime.now().minusHours(2));
        firstComment = commentRepository.save(firstComment);

        // Second comment for testCard (created by testUser) - newer
        secondComment = new CommentEntity();
        secondComment.setMessage("Second Comment");
        secondComment.setCreatedBy(testUser);
        secondComment.setCard(testCard);
        secondComment.setCreatedAt(LocalDateTime.now().minusHours(1));
        secondComment = commentRepository.save(secondComment);

        // Third comment for secondCard (created by otherUser)
        thirdComment = new CommentEntity();
        thirdComment.setMessage("Third Comment");
        thirdComment.setCreatedBy(otherUser);
        thirdComment.setCard(secondCard);
        thirdComment.setCreatedAt(LocalDateTime.now());
        thirdComment = commentRepository.save(thirdComment);
    }
}
