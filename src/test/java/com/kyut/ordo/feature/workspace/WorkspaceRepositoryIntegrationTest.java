package com.kyut.ordo.feature.workspace;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Workspace repositories using PostgreSQL with Testcontainers
 */
@Import(TestConfig.class)
@DisplayName("Workspace Repository Integration Tests with PostgreSQL")
@Transactional
class WorkspaceRepositoryIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private WorkspaceRoleRepository workspaceRoleRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private WorkspaceEntity otherWorkspace;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test users
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        otherUser = dataBuilder.createTestUser("other@example.com", "Other User");

        // Create test workspaces
        testWorkspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);
        otherWorkspace = dataBuilder.createTestWorkspace("Other Workspace", "Other Description", otherUser);
    }

    @Test
    @DisplayName("Find workspaces by owner")
    void findAllByOwner() {
        // When
        Page<WorkspaceEntity> result = workspaceRepository.findAllByOwner(testUser, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Workspace");
        assertThat(result.getContent().get(0).getOwner().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Find workspaces by owner - no workspaces")
    void findAllByOwner_NoWorkspaces() {
        // Given
        UserEntity userWithoutWorkspaces = dataBuilder.createTestUser("empty@example.com", "Empty User");

        // When
        Page<WorkspaceEntity> result = workspaceRepository.findAllByOwner(userWithoutWorkspaces, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Find workspaces by member user")
    void findAllByMembersUser() {
        // When
        Page<WorkspaceEntity> result = workspaceRepository.findAllByMembersUser(testUser, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Workspace");
    }

    @Test
    @DisplayName("Find joined workspaces (excluding owned)")
    void findAllJoinedByMember() {
        // Given - Add testUser as member to otherWorkspace
        dataBuilder.addUserToWorkspaceWithRole(otherWorkspace, testUser, "Member");

        // When
        Page<WorkspaceEntity> result = workspaceRepository.findAllJoinedByMember(testUser, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Other Workspace");
        assertThat(result.getContent().get(0).getOwner().getId()).isEqualTo(otherUser.getId());
    }

    @Test
    @DisplayName("Find joined workspaces - owner only")
    void findAllJoinedByMember_OwnerOnly() {
        // When - testUser is only owner, not member of other workspaces
        Page<WorkspaceEntity> result = workspaceRepository.findAllJoinedByMember(testUser, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Find workspace member by workspace and user")
    void findByWorkspaceAndUser() {
        // When
        Optional<WorkspaceMemberEntity> result = workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getWorkspace().getId()).isEqualTo(testWorkspace.getId());
        assertThat(result.get().getRole().getName()).isEqualTo("Owner");
    }

    @Test
    @DisplayName("Find workspace member by workspace and user - not found")
    void findByWorkspaceAndUser_NotFound() {
        // When - otherUser is not a member of testWorkspace
        Optional<WorkspaceMemberEntity> result = workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, otherUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find workspace member by workspace ID and user ID")
    void findByWorkspaceIdAndUserId() {
        // When
        Optional<WorkspaceMemberEntity> result = workspaceMemberRepository.findByWorkspaceIdAndUserId(
                testWorkspace.getId(), testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getWorkspace().getId()).isEqualTo(testWorkspace.getId());
    }

    @Test
    @DisplayName("Find all workspace members by workspace")
    void findAllByWorkspace() {
        // Given - Add another member
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");

        // When
        Page<WorkspaceMemberEntity> result = workspaceMemberRepository.findAllByWorkspace(testWorkspace, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        
        List<String> userEmails = result.getContent().stream()
                .map(member -> member.getUser().getEmail())
                .toList();
        assertThat(userEmails).containsExactlyInAnyOrder("test@example.com", "other@example.com");
    }

    @Test
    @DisplayName("Find all workspace roles by workspace")
    void findAllWorkspaceRolesByWorkspace() {
        // When
        Page<WorkspaceRoleEntity> result = workspaceRoleRepository.findAllByWorkspace(testWorkspace, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(3); // Owner, Member, Guest roles
        
        List<String> roleNames = result.getContent().stream()
                .map(WorkspaceRoleEntity::getName)
                .toList();
        assertThat(roleNames).containsExactlyInAnyOrder("Owner", "Member", "Guest");
    }

    @Test
    @DisplayName("Delete all workspace members by workspace")
    void deleteAllByWorkspace() {
        // Given - Add another member
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");
        
        // Verify members exist
        Page<WorkspaceMemberEntity> beforeDelete = workspaceMemberRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged());
        assertThat(beforeDelete.getContent()).hasSize(2);

        // When
        workspaceMemberRepository.deleteAllByWorkspace(testWorkspace);

        // Then
        Page<WorkspaceMemberEntity> afterDelete = workspaceMemberRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged());
        assertThat(afterDelete.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Workspace pagination")
    void testWorkspacePagination() {
        // Given - Create multiple workspaces for testUser
        for (int i = 0; i < 5; i++) {
            dataBuilder.createTestWorkspace("Workspace " + i, "Description " + i, testUser);
        }

        // When - Request first page with 3 items
        Page<WorkspaceEntity> firstPage = workspaceRepository.findAllByOwner(testUser, PageRequest.of(0, 3));

        // Then
        assertThat(firstPage.getContent()).hasSize(3);
        assertThat(firstPage.getTotalElements()).isEqualTo(6); // 5 new + 1 original
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.hasPrevious()).isFalse();

        // When - Request second page
        Page<WorkspaceEntity> secondPage = workspaceRepository.findAllByOwner(testUser, PageRequest.of(1, 3));

        // Then
        assertThat(secondPage.getContent()).hasSize(3);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Complex workspace membership scenario")
    void complexMembershipScenario() {
        // Given - Create additional users and workspaces
        UserEntity thirdUser = dataBuilder.createTestUser("third@example.com", "Third User");
        WorkspaceEntity sharedWorkspace = dataBuilder.createTestWorkspace("Shared Workspace", "Shared", thirdUser);

        // Add testUser and otherUser as members to sharedWorkspace
        dataBuilder.addUserToWorkspaceWithRole(sharedWorkspace, testUser, "Member");
        dataBuilder.addUserToWorkspaceWithRole(sharedWorkspace, otherUser, "Guest");

        // When - Get all workspaces where testUser is a member
        Page<WorkspaceEntity> testUserWorkspaces = workspaceRepository.findAllByMembersUser(testUser, Pageable.unpaged());

        // Then
        assertThat(testUserWorkspaces.getContent()).hasSize(2); // testWorkspace (owned) + sharedWorkspace (member)
        
        List<String> workspaceTitles = testUserWorkspaces.getContent().stream()
                .map(WorkspaceEntity::getTitle)
                .toList();
        assertThat(workspaceTitles).containsExactlyInAnyOrder("Test Workspace", "Shared Workspace");

        // When - Get only joined workspaces (not owned)
        Page<WorkspaceEntity> joinedWorkspaces = workspaceRepository.findAllJoinedByMember(testUser, Pageable.unpaged());

        // Then
        assertThat(joinedWorkspaces.getContent()).hasSize(1);
        assertThat(joinedWorkspaces.getContent().get(0).getTitle()).isEqualTo("Shared Workspace");
    }

    @Test
    @DisplayName("Workspace role permissions verification")
    void workspaceRolePermissions() {
        // When - Get roles from test workspace
        Page<WorkspaceRoleEntity> roles = workspaceRoleRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged());

        // Then - Verify Owner role permissions
        WorkspaceRoleEntity ownerRole = roles.getContent().stream()
                .filter(role -> "Owner".equals(role.getName()))
                .findFirst()
                .orElseThrow();
        
        assertThat(ownerRole.isAbleToManageSettings()).isTrue();
        assertThat(ownerRole.isAbleToManageMembers()).isTrue();
        assertThat(ownerRole.isAbleToManageContent()).isTrue();
        assertThat(ownerRole.isAbleToManageRoles()).isTrue();

        // Then - Verify Member role permissions
        WorkspaceRoleEntity memberRole = roles.getContent().stream()
                .filter(role -> "Member".equals(role.getName()))
                .findFirst()
                .orElseThrow();
        
        assertThat(memberRole.isAbleToManageSettings()).isFalse();
        assertThat(memberRole.isAbleToManageMembers()).isFalse();
        assertThat(memberRole.isAbleToManageContent()).isTrue();
        assertThat(memberRole.isAbleToManageRoles()).isFalse();

        // Then - Verify Guest role permissions
        WorkspaceRoleEntity guestRole = roles.getContent().stream()
                .filter(role -> "Guest".equals(role.getName()))
                .findFirst()
                .orElseThrow();
        
        assertThat(guestRole.isAbleToManageSettings()).isFalse();
        assertThat(guestRole.isAbleToManageMembers()).isFalse();
        assertThat(guestRole.isAbleToManageContent()).isFalse();
        assertThat(guestRole.isAbleToManageRoles()).isFalse();
    }
}
