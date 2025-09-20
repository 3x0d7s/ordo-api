package com.kyut.ordo.feature.board;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BoardPermissionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardPermissionService Unit Tests")
class BoardPermissionServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;
    
    @Mock
    private BoardRoleRepository boardRoleRepository;

    @InjectMocks
    private BoardPermissionService boardPermissionService;

    private UserEntity testUser;
    private BoardEntity testBoard;
    private BoardRoleEntity adminRole;
    private BoardRoleEntity memberRole;
    private BoardMemberEntity testMember;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");

        testBoard = new BoardEntity();
        testBoard.setId(1L);
        testBoard.setTitle("Test Board");

        // Create admin role with all permissions
        adminRole = new BoardRoleEntity();
        adminRole.setId(1L);
        adminRole.setName("Admin");
        adminRole.setBoard(testBoard);
        adminRole.setAbleToEdit(true);
        adminRole.setAbleToDelete(true);
        adminRole.setAbleToInviteMembers(true);
        adminRole.setAbleToManageRoles(true);
        adminRole.setAbleToCreateLists(true);
        adminRole.setAbleToCreateTasks(true);

        // Create member role with limited permissions
        memberRole = new BoardRoleEntity();
        memberRole.setId(2L);
        memberRole.setName("Member");
        memberRole.setBoard(testBoard);
        memberRole.setAbleToEdit(false);
        memberRole.setAbleToDelete(false);
        memberRole.setAbleToInviteMembers(false);
        memberRole.setAbleToManageRoles(false);
        memberRole.setAbleToCreateLists(true);
        memberRole.setAbleToCreateTasks(true);

        testMember = new BoardMemberEntity();
        testMember.setId(1L);
        testMember.setUser(testUser);
        testMember.setBoard(testBoard);
        testMember.setRole(adminRole);
    }

    @Test
    @DisplayName("Has permission - user is admin and has all permissions")
    void hasPermission_UserIsAdminWithAllPermissions() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.of(testMember));

        // When & Then
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "DELETE")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "INVITE")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).isTrue();

        verify(boardMemberRepository).findByBoardIdAndUserId(1L, testUser.getId());
    }

    @Test
    @DisplayName("Has permission - user is member with limited permissions")
    void hasPermission_UserIsMemberWithLimitedPermissions() {
        // Given
        testMember.setRole(memberRole); // Set to member role
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.of(testMember));

        // When & Then
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT")).isFalse();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "DELETE")).isFalse();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "INVITE")).isFalse();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "MANAGE_ROLES")).isFalse();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_LISTS")).isTrue();
        assertThat(boardPermissionService.hasPermission(1L, testUser.getId(), "CREATE_TASKS")).isTrue();
    }

    @Test
    @DisplayName("Has permission - user is not a member")
    void hasPermission_UserIsNotMember() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.empty());

        // When
        boolean result = boardPermissionService.hasPermission(1L, testUser.getId(), "EDIT");

        // Then
        assertThat(result).isFalse();
        verify(boardMemberRepository).findByBoardIdAndUserId(1L, testUser.getId());
    }

    @Test
    @DisplayName("Has permission - unknown permission")
    void hasPermission_UnknownPermission() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.of(testMember));

        // When
        boolean result = boardPermissionService.hasPermission(1L, testUser.getId(), "UNKNOWN_PERMISSION");

        // Then
        assertThat(result).isFalse();
        verify(boardMemberRepository).findByBoardIdAndUserId(1L, testUser.getId());
    }

    @Test
    @DisplayName("Add member - success")
    void addMember_Success() {
        // Given
        UserEntity newUser = TestConfig.TestDataFactory.createTestUserWithId(2L, "new@example.com", "New User");
        BoardMemberEntity expectedMember = BoardMemberEntity.builder()
                .board(testBoard)
                .user(newUser)
                .role(memberRole)
                .joinedAt(LocalDateTime.now())
                .build();

        when(boardMemberRepository.save(any(BoardMemberEntity.class))).thenReturn(expectedMember);

        // When
        BoardMemberEntity result = boardPermissionService.addMember(testBoard, newUser, memberRole);

        // Then
        assertThat(result).isNotNull();
        verify(boardMemberRepository).save(any(BoardMemberEntity.class));
    }

    @Test
    @DisplayName("Update member role - success")
    void updateMemberRole_Success() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.of(testMember));
        when(boardRoleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(boardMemberRepository.save(testMember)).thenReturn(testMember);

        // When
        BoardMemberEntity result = boardPermissionService.updateMemberRole(1L, testUser.getId(), 2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(memberRole);
        verify(boardMemberRepository).findByBoardIdAndUserId(1L, testUser.getId());
        verify(boardRoleRepository).findById(2L);
        verify(boardMemberRepository).save(testMember);
    }

    @Test
    @DisplayName("Update member role - member not found")
    void updateMemberRole_MemberNotFound() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.empty());

        // When & Then
        try {
            boardPermissionService.updateMemberRole(1L, testUser.getId(), 2L);
            assertThat(false).as("Expected IllegalArgumentException").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Member not found");
        }
    }

    @Test
    @DisplayName("Update member role - role not found")
    void updateMemberRole_RoleNotFound() {
        // Given
        when(boardMemberRepository.findByBoardIdAndUserId(1L, testUser.getId()))
            .thenReturn(Optional.of(testMember));
        when(boardRoleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        try {
            boardPermissionService.updateMemberRole(1L, testUser.getId(), 999L);
            assertThat(false).as("Expected IllegalArgumentException").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Role not found");
        }
    }

    @Test
    @DisplayName("Get board members - success")
    void getBoardMembers_Success() {
        // Given
        List<BoardMemberEntity> expectedMembers = List.of(testMember);
        when(boardMemberRepository.findAllByBoardId(1L)).thenReturn(expectedMembers);

        // When
        List<BoardMemberEntity> result = boardPermissionService.getBoardMembers(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testMember);
        verify(boardMemberRepository).findAllByBoardId(1L);
    }
}