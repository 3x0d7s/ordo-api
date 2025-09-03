package com.kyut.ordo.feature.list.repository;

import com.kyut.ordo.security.auth.common.AuthProvider;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration тести для ListRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("ListRepository Integration Tests")
class ListRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ListRepository listRepository;

    private BoardEntity testBoard;

    @BeforeEach
    void setUp() {
        // Створюємо тестового користувача
        UserEntity testUser = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .password("$2a$10$test.password.hash")
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        // Створюємо тестову дошку
        testBoard = new BoardEntity();
        testBoard.setTitle("Test Board");
        testBoard.setDescription("Test Description");
        testBoard.setVisibility(BoardVisibility.PRIVATE);
        testBoard.setCreatedAt(LocalDateTime.now());
        testBoard = entityManager.persistAndFlush(testBoard);
    }

    @Test
    @DisplayName("Знаходження списків по дошці з сортуванням по позиції")
    void findAllByBoardOrderByPosition() {
        // Given
        ListEntity list1 = createList("List 1", 2);
        ListEntity list2 = createList("List 2", 0);
        ListEntity list3 = createList("List 3", 1);
        
        entityManager.persistAndFlush(list1);
        entityManager.persistAndFlush(list2);
        entityManager.persistAndFlush(list3);

        // When
        List<ListEntity> result = listRepository.findAllByBoardOrderByPosition(testBoard);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("List 2"); // position 0
        assertThat(result.get(1).getTitle()).isEqualTo("List 3"); // position 1
        assertThat(result.get(2).getTitle()).isEqualTo("List 1"); // position 2
    }

    @Test
    @DisplayName("Підрахунок кількості списків в дошці")
    void countByBoard() {
        // Given
        createAndPersistList("List 1", 0);
        createAndPersistList("List 2", 1);
        createAndPersistList("List 3", 2);

        // When
        Integer count = listRepository.countByBoard(testBoard);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Підрахунок кількості списків в порожній дошці")
    void countByBoard_EmptyBoard() {
        // When
        Integer count = listRepository.countByBoard(testBoard);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Видалення списку по ID")
    void deleteListById() {
        // Given
        ListEntity list = createAndPersistList("Test List", 0);
        Long listId = list.getId();

        // When
        listRepository.deleteById(listId); // Використовуємо стандартний метод JPA
        entityManager.flush();

        // Then
        assertThat(listRepository.findById(listId)).isEmpty();
    }

    private ListEntity createList(String title, int position) {
        ListEntity list = new ListEntity();
        list.setTitle(title);
        list.setPosition(position);
        list.setBoard(testBoard);
        list.setCreatedAt(LocalDateTime.now());
        return list;
    }

    private ListEntity createAndPersistList(String title, int position) {
        ListEntity list = createList(title, position);
        return entityManager.persistAndFlush(list);
    }
}
