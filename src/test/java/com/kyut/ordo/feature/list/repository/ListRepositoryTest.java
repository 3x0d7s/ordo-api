package com.kyut.ordo.feature.list.repository;

import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import com.kyut.ordo.testcontainers.PostgreSQLTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ListRepository using PostgreSQL with Testcontainers
 */
@Import(TestConfig.class)
@DisplayName("ListRepository Integration Tests with PostgreSQL")
@Transactional
class ListRepositoryTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    private BoardEntity testBoard;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        dataBuilder.createTestUser("test@example.com", "name");

        testBoard = dataBuilder.createTestBoard("Test Board", "Test Description");
    }

    @Test
    @DisplayName("Find lists by board ordered by position")
    void findAllByBoardOrderByPosition() {
        // Given
        ListEntity list1 = createList("List 1", 2);
        ListEntity list2 = createList("List 2", 0);
        ListEntity list3 = createList("List 3", 1);
        
        listRepository.save(list1);
        listRepository.save(list2);
        listRepository.save(list3);

        // When
        List<ListEntity> result = listRepository.findAllByBoardOrderByPosition(testBoard);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("List 2"); // position 0
        assertThat(result.get(1).getTitle()).isEqualTo("List 3"); // position 1
        assertThat(result.get(2).getTitle()).isEqualTo("List 1"); // position 2
    }

    @Test
    @DisplayName("Count lists in board")
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
    @DisplayName("Count lists in empty board")
    void countByBoard_EmptyBoard() {
        // When
        Integer count = listRepository.countByBoard(testBoard);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Delete list by ID")
    void deleteListById() {
        // Given
        ListEntity list = createAndPersistList("Test List", 0);
        Long listId = list.getId();

        // When
        listRepository.deleteById(listId); // Use standard JPA method

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
        return listRepository.save(list);
    }
}
