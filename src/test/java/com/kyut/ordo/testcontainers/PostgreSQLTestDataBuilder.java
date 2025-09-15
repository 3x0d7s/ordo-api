package com.kyut.ordo.testcontainers;

import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.board.service.BoardRoleFactory;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.feature.workspace.service.WorkspaceRoleFactory;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

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
    
    @Autowired
    private BoardRoleRepository boardRoleRepository;
    
    @Autowired
    private BoardMemberRepository boardMemberRepository;
    
    @Autowired
    private BoardRoleFactory boardRoleFactory;
    
    @Autowired
    private BoardPermissionService boardPermissionService;
    
    @Autowired
    private WorkspaceRepository workspaceRepository;
    
    @Autowired
    private WorkspaceRoleRepository workspaceRoleRepository;
    
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    
    @Autowired
    private WorkspaceRoleFactory workspaceRoleFactory;

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
     * Creates and saves a test board with a user as owner
     */
    public BoardEntity createTestBoardWithOwner(String title, String description, UserEntity owner) {
        BoardEntity board = createTestBoard(title, description);
        
        // Create roles for the board
        Map<String, BoardRoleEntity> roles = boardRoleFactory.rolesAsMap(board);
        
        // Add the user as owner of the board
        boardPermissionService.addMember(board, owner, roles.get("Owner"));
        
        return board;
    }

    /**
     * Adds a user as a member to a board with specified role
     */
    public BoardMemberEntity addUserToBoardWithRole(BoardEntity board, UserEntity user, String roleName) {
        // Find or create the role
        BoardRoleEntity role = boardRoleRepository.findByBoardAndName(board, roleName)
            .orElseGet(() -> {
                switch (roleName) {
                    case "Owner" -> {
                        return boardRoleFactory.createOwnerRole(board);
                    }
                    case "Member" -> {
                        return boardRoleFactory.createMemberRole(board);
                    }
                    case "Guest" -> {
                        return boardRoleFactory.createGuestRole(board);
                    }
                    default -> throw new IllegalArgumentException("Unknown role: " + roleName);
                }
            });
        
        return boardPermissionService.addMember(board, user, role);
    }

    /**
     * Creates and saves a test workspace
     */
    public WorkspaceEntity createTestWorkspace(String title, String description, UserEntity owner) {
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setTitle(title);
        workspace.setDescription(description);
        workspace.setOwner(owner);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace = workspaceRepository.save(workspace);

        // Create roles for the workspace
        Map<String, WorkspaceRoleEntity> roles = workspaceRoleFactory.rolesAsMap(workspace);

        // Add the user as owner of the workspace
        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(owner)
                .role(roles.get("Owner"))
                .joinedAt(LocalDateTime.now())
                .build();
        
        workspaceMemberRepository.save(member);

        return workspace;
    }

    /**
     * Adds a user as a member to a workspace with specified role
     */
    public WorkspaceMemberEntity addUserToWorkspaceWithRole(WorkspaceEntity workspace, UserEntity user, String roleName) {
        // Find or create the role
        WorkspaceRoleEntity role = workspaceRoleRepository.findAllByWorkspace(workspace, Pageable.unpaged())
            .getContent().stream()
            .filter(r -> roleName.equals(r.getName()))
            .findFirst()
            .orElseGet(() -> {
                switch (roleName) {
                    case "Owner" -> {
                        return workspaceRoleFactory.createOwnerRole(workspace);
                    }
                    case "Member" -> {
                        return workspaceRoleFactory.createMemberRole(workspace);
                    }
                    case "Guest" -> {
                        return workspaceRoleFactory.createGuestRole(workspace);
                    }
                    default -> throw new IllegalArgumentException("Unknown role: " + roleName);
                }
            });

        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        return workspaceMemberRepository.save(member);
    }

    /**
     * Creates and saves a test board with workspace
     */
    public BoardEntity createTestBoardWithWorkspace(String title, String description, UserEntity owner, WorkspaceEntity workspace) {
        BoardEntity board = new BoardEntity();
        board.setTitle(title);
        board.setDescription(description);
        board.setVisibility(BoardVisibility.WORKSPACE);
        board.setWorkspace(workspace);
        board.setCreatedAt(LocalDateTime.now());
        board = boardRepository.save(board);
        
        // Create roles for the board
        Map<String, BoardRoleEntity> roles = boardRoleFactory.rolesAsMap(board);
        
        // Add the user as owner of the board
        boardPermissionService.addMember(board, owner, roles.get("Owner"));
        
        return board;
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
        boardMemberRepository.deleteAll();
        boardRoleRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRoleRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
    }
}
