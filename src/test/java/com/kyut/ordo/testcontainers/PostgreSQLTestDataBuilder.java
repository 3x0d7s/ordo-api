package com.kyut.ordo.testcontainers;

import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Utility class for creating test data in PostgreSQL integration tests.
 * This builder pattern makes it easier to create test entities for integration tests.
 */
@Component
@Transactional
public class PostgreSQLTestDataBuilder {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Autowired
    private ListRepository listRepository;

    /**
     * Creates and saves a test user
     */
    public UserEntity createTestUser(String email, String name) {
        UserEntity user = UserEntity.builder()
                .email(email)
                .name(name)
                .password("$2a$10$test.password.hash")
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    /**
     * Creates and saves a test board
     */
    public BoardEntity createTestBoard(String title, String description) {
        BoardEntity board = new BoardEntity();
        board.setTitle(title);
        board.setDescription(description);
        board.setVisibility(BoardVisibility.PRIVATE);
        board.setCreatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    /**
     * Creates and saves a test list for a given board
     */
    public ListEntity createTestList(String title, int position, BoardEntity board) {
        ListEntity list = new ListEntity();
        list.setTitle(title);
        list.setPosition(position);
        list.setBoard(board);
        list.setCreatedAt(LocalDateTime.now());
        return listRepository.save(list);
    }

    /**
     * Cleans all test data from the database
     */
    public void cleanAllData() {
        listRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }
}
