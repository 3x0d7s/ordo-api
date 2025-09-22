package com.kyut.ordo.feature.card;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CardRepository with PostgreSQL database.
 * Tests custom repository methods and JPA functionality.
 */
@AutoConfigureWebMvc
@Import({TestConfig.class, PostgreSQLTestDataBuilder.class})
@DisplayName("CardRepository Integration Tests with PostgreSQL")
@Transactional
class CardRepositoryIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private BoardEntity testBoard;
    private ListEntity testList;
    private ListEntity secondList;
    private CardEntity testCard1;
    private CardEntity testCard2;
    private CardEntity testCard3;

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

        // Create test lists
        testList = dataBuilder.createTestList("Test List 1", 0, testBoard);
        secondList = dataBuilder.createTestList("Test List 2", 1, testBoard);

        // Create test cards
        testCard1 = createCard("Card 1", "Description 1", 0, testList, testUser, testUser);
        testCard2 = createCard("Card 2", "Description 2", 1, testList, testUser, otherUser);
        testCard3 = createCard("Card 3", "Description 3", 0, secondList, testUser, null);
    }

    @Test
    @DisplayName("Find all cards by list - should return cards ordered by position")
    void findAllByList_ShouldReturnCardsOrderedByPosition() {
        // When
        List<CardEntity> cards = cardRepository.findAllByList(testList);

        // Then
        assertThat(cards).isNotNull();
        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).getTitle()).isEqualTo("Card 1");
        assertThat(cards.get(1).getTitle()).isEqualTo("Card 2");
    }

    @Test
    @DisplayName("Find all cards by list ordered by position - should return cards in correct order")
    void findAllByListOrderByPosition_ShouldReturnCardsInCorrectOrder() {
        // When
        List<CardEntity> cards = cardRepository.findAllByListOrderByPosition(testList);

        // Then
        assertThat(cards).isNotNull();
        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).getPosition()).isZero();
        assertThat(cards.get(1).getPosition()).isEqualTo(1);
        assertThat(cards.get(0).getTitle()).isEqualTo("Card 1");
        assertThat(cards.get(1).getTitle()).isEqualTo("Card 2");
    }

    @Test
    @DisplayName("Find all cards by list with pagination - should return paginated results")
    void findAllByListWithPagination_ShouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<CardEntity> cards = cardRepository.findAllByList(testList, pageable);

        // Then
        assertThat(cards).isNotNull();
        assertThat(cards.getContent()).hasSize(1);
        assertThat(cards.getTotalElements()).isEqualTo(2);
        assertThat(cards.getTotalPages()).isEqualTo(2);
        assertThat(cards.getContent().get(0).getTitle()).isEqualTo("Card 1");
    }

    @Test
    @DisplayName("Find card by ID and list - should return card when exists in list")
    void findByIdAndList_ShouldReturnCardWhenExistsInList() {
        // When
        Optional<CardEntity> card = cardRepository.findByIdAndList(testCard1.getId(), testList);

        // Then
        assertThat(card).isPresent();
        assertThat(card.get().getTitle()).isEqualTo("Card 1");
    }

    @Test
    @DisplayName("Find card by ID and list - should return empty when card not in list")
    void findByIdAndList_ShouldReturnEmptyWhenCardNotInList() {
        // When
        Optional<CardEntity> card = cardRepository.findByIdAndList(testCard3.getId(), testList);

        // Then
        assertThat(card).isEmpty();
    }

    @Test
    @DisplayName("Find all cards assigned to user - should return user's assigned cards")
    void findAllByAssignedTo_ShouldReturnUsersAssignedCards() {
        // When
        List<CardEntity> cards = cardRepository.findAllByAssignedTo(testUser);

        // Then
        assertThat(cards).isNotNull();
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getTitle()).isEqualTo("Card 1");
        assertThat(cards.get(0).getAssignedTo()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Find all cards assigned to user with pagination - should return paginated assigned cards")
    void findAllByAssignedToWithPagination_ShouldReturnPaginatedAssignedCards() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CardEntity> cards = cardRepository.findAllByAssignedTo(otherUser, pageable);

        // Then
        assertThat(cards).isNotNull();
        assertThat(cards.getContent()).hasSize(1);
        assertThat(cards.getContent().get(0).getTitle()).isEqualTo("Card 2");
        assertThat(cards.getContent().get(0).getAssignedTo()).isEqualTo(otherUser);
    }

    @Test
    @DisplayName("Count cards by list - should return correct count")
    void countByList_ShouldReturnCorrectCount() {
        // When
        Integer count = cardRepository.countByList(testList);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Count cards by empty list - should return zero")
    void countByList_ShouldReturnZeroForEmptyList() {
        // Given
        ListEntity emptyList = dataBuilder.createTestList("Empty List", 2, testBoard);

        // When
        Integer count = cardRepository.countByList(emptyList);

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Delete all cards by list - should remove all cards from list")
    void deleteAllByList_ShouldRemoveAllCardsFromList() {
        // Given
        Integer initialCount = cardRepository.countByList(testList);
        assertThat(initialCount).isEqualTo(2);

        // When
        cardRepository.deleteAllByList(testList);

        // Then
        Integer finalCount = cardRepository.countByList(testList);
        assertThat(finalCount).isZero();

        // Verify cards from other lists are not affected
        List<CardEntity> remainingCards = cardRepository.findAllByList(secondList);
        assertThat(remainingCards).hasSize(1);
        assertThat(remainingCards.get(0).getTitle()).isEqualTo("Card 3");
    }

    @Test
    @DisplayName("Find cards with null assigned user - should handle null assignments")
    void findAllByAssignedTo_ShouldHandleNullAssignments() {
        // When
        List<CardEntity> cards = cardRepository.findAllByAssignedTo(null);

        // Then
        // This should return empty list or handle null gracefully
        // depending on JPA implementation
        assertThat(cards).isNotNull();
    }

    private CardEntity createCard(String title, String description, Integer position, 
                                 ListEntity list, UserEntity createdBy, UserEntity assignedTo) {
        CardEntity card = new CardEntity();
        card.setTitle(title);
        card.setDescription(description);
        card.setDueDate(LocalDate.now().plusDays(7));
        card.setPosition(position);
        card.setList(list);
        card.setCreatedBy(createdBy);
        card.setAssignedTo(assignedTo);
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }
}