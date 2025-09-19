package com.kyut.ordo.feature.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import com.kyut.ordo.testcontainers.PostgreSQLTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for WorkspaceController using PostgreSQL with Testcontainers.
 * Tests the full HTTP request/response cycle including authentication, validation, and database operations.
 */
@AutoConfigureWebMvc
@Import(TestConfig.class)
@DisplayName("WorkspaceController Integration Tests with PostgreSQL")
@Transactional
class WorkspaceControllerIntegrationTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private WorkspaceRoleRepository workspaceRoleRepository;

    @Autowired
    private PostgreSQLTestDataBuilder dataBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private WorkspaceRoleEntity memberRole;

    @BeforeEach
    void setUp() {
        dataBuilder.cleanAllData();

        // Create test users
        testUser = dataBuilder.createTestUser("test@example.com", "Test User");
        otherUser = dataBuilder.createTestUser("other@example.com", "Other User");

        // Create test workspace with user as owner
        testWorkspace = dataBuilder.createTestWorkspace("Test Workspace", "Test Description", testUser);

        // Get member role for tests - find it among the created roles
        memberRole = workspaceRoleRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged())
                .getContent().stream()
                .filter(role -> "Member".equals(role.getName()))
                .findFirst().orElse(null);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create workspace - success scenario")
    void createWorkspace_Success() throws Exception {
        // Given
        WorkspaceCreate workspaceCreate = new WorkspaceCreate("New Workspace", "New Description");

        // When & Then
        mockMvc.perform(post("/workspaces")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Workspace"))
                .andExpect(jsonPath("$.description").value("New Description"));

        // Verify workspace was saved in database
        assertThat(workspaceRepository.findAll()).hasSize(2); // Original + new
    }

    @Test
    @DisplayName("Get workspace by ID - success scenario")
    void getWorkspaceById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/{id}", testWorkspace.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWorkspace.getId()))
                .andExpect(jsonPath("$.title").value("Test Workspace"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Get workspace by ID - workspace not found")
    void getWorkspaceById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/{id}", 999L)
                .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update workspace - success scenario")
    void updateWorkspace_Success() throws Exception {
        // Given
        WorkspaceUpdate workspaceUpdate = new WorkspaceUpdate("Updated Workspace", "Updated Description");

        // When & Then
        mockMvc.perform(put("/workspaces/{id}", testWorkspace.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Workspace"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Verify workspace was updated in database
        WorkspaceEntity updatedWorkspace = workspaceRepository.findById(testWorkspace.getId()).orElseThrow();
        assertThat(updatedWorkspace.getTitle()).isEqualTo("Updated Workspace");
        assertThat(updatedWorkspace.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("Update workspace - insufficient permissions")
    void updateWorkspace_InsufficientPermissions() throws Exception {
        // Given
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");
        WorkspaceUpdate workspaceUpdate = new WorkspaceUpdate("Updated Workspace", "Updated Description");

        // When & Then
        mockMvc.perform(put("/workspaces/{id}", testWorkspace.getId())
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceUpdate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete workspace - success scenario")
    void deleteWorkspace_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/workspaces/{id}", testWorkspace.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWorkspace.getId()))
                .andExpect(jsonPath("$.title").value("Test Workspace"));

        // Verify workspace was deleted from database
        assertThat(workspaceRepository.findById(testWorkspace.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delete workspace - insufficient permissions")
    void deleteWorkspace_InsufficientPermissions() throws Exception {
        // Given
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");

        // When & Then
        mockMvc.perform(delete("/workspaces/{id}", testWorkspace.getId())
                .with(user(otherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get workspaces by member - success scenario")
    void getWorkspacesByMember_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces")
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Get owned workspaces - success scenario")
    void getOwnedWorkspaces_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/owned")
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Get joined workspaces - success scenario")
    void getJoinedWorkspaces_Success() throws Exception {
        // Given - Add otherUser as member to testWorkspace
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");

        // When & Then
        mockMvc.perform(get("/workspaces/joined")
                .with(user(otherUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Get workspace roles - success scenario")
    void getWorkspaceRoles_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/{id}/roles", testWorkspace.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3)); // Owner, Member, Guest
    }

    @Test
    @DisplayName("Create workspace role - success scenario")
    void createWorkspaceRole_Success() throws Exception {
        // Given
        WorkspaceRoleCreate roleCreate = new WorkspaceRoleCreate();
        roleCreate.setWorkspaceId(testWorkspace.getId());
        roleCreate.setName("Custom Role");
        roleCreate.setAbleToManageContent(true);
        roleCreate.setAbleToManageMembers(false);
        roleCreate.setAbleToManageSettings(false);
        roleCreate.setAbleToManageRoles(false);

        // When & Then
        mockMvc.perform(post("/workspaces/{id}/roles", testWorkspace.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Custom Role"))
                .andExpect(jsonPath("$.ableToManageContent").value(true))
                .andExpect(jsonPath("$.ableToManageMembers").value(false));

        // Verify role was saved in database
        assertThat(workspaceRoleRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged())
                .getContent().stream()
                .anyMatch(role -> "Custom Role".equals(role.getName()))).isTrue();
    }

    @Test
    @DisplayName("Update workspace role - success scenario")
    void updateWorkspaceRole_Success() throws Exception {
        // Given
        WorkspaceRoleUpdate roleUpdate = new WorkspaceRoleUpdate();
        roleUpdate.setName("Updated Role");
        roleUpdate.setAbleToManageContent(false);
        roleUpdate.setAbleToManageMembers(true);
        roleUpdate.setAbleToManageSettings(false);
        roleUpdate.setAbleToManageRoles(false);

        // When & Then
        mockMvc.perform(put("/workspaces/{id}/roles/{roleId}", testWorkspace.getId(), memberRole.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Role"))
                .andExpect(jsonPath("$.ableToManageContent").value(false))
                .andExpect(jsonPath("$.ableToManageMembers").value(true));

        // Verify role was updated in database
        WorkspaceRoleEntity updatedRole = workspaceRoleRepository.findById(memberRole.getId()).orElseThrow();
        assertThat(updatedRole.getName()).isEqualTo("Updated Role");
        assertThat(updatedRole.isAbleToManageContent()).isFalse();
        assertThat(updatedRole.isAbleToManageMembers()).isTrue();
    }

    @Test
    @DisplayName("Get my role in workspace - success scenario")
    void getMyRole_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/{id}/my-role", testWorkspace.getId())
                .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Owner"))
                .andExpect(jsonPath("$.ableToManageSettings").value(true));
    }

    @Test
    @DisplayName("Get workspace members - success scenario")
    void getWorkspaceMembers_Success() throws Exception {
        // Given - Add another member
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");

        // When & Then
        mockMvc.perform(get("/workspaces/{id}/members", testWorkspace.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2)); // testUser as Owner + otherUser as Member
    }

    @Test
    @DisplayName("Create workspace member - success scenario")
    void createWorkspaceMember_Success() throws Exception {
        // Given
        WorkspaceMemberCreate memberCreate = new WorkspaceMemberCreate();
        memberCreate.setUserId(otherUser.getId());
        memberCreate.setWorkspaceId(testWorkspace.getId());
        memberCreate.setWorkspaceRoleId(memberRole.getId());

        // When & Then
        mockMvc.perform(post("/workspaces/{id}/members", testWorkspace.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberCreate)))
                .andDo(handler ->
                                System.out.println(handler.getResponse().getContentAsString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(otherUser.getId()))
                .andExpect(jsonPath("$.role.id").value(memberRole.getId()));

        // Verify member was added to database
        assertThat(workspaceMemberRepository.findByWorkspaceIdAndUserId(testWorkspace.getId(), otherUser.getId())).isPresent();
    }

    @Test
    @DisplayName("Update workspace member - success scenario")
    void updateWorkspaceMember_Success() throws Exception {
        // Given - Add member first
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");
        WorkspaceRoleEntity guestRole = workspaceRoleRepository.findAllByWorkspace(testWorkspace, Pageable.unpaged())
                .getContent().stream()
                .filter(role -> "Guest".equals(role.getName()))
                .findFirst().orElseThrow();
        
        WorkspaceMemberUpdate memberUpdate = new WorkspaceMemberUpdate();
        memberUpdate.setWorkspaceId(testWorkspace.getId());
        memberUpdate.setWorkspaceRoleId(guestRole.getId());

        // When & Then
        mockMvc.perform(put("/workspaces/{id}/members/{userId}", testWorkspace.getId(), otherUser.getId())
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role.id").value(guestRole.getId()));

        // Verify member role was updated in database
        WorkspaceMemberEntity updatedMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(testWorkspace.getId(), otherUser.getId()).orElseThrow();
        assertThat(updatedMember.getRole().getId()).isEqualTo(guestRole.getId());
    }

    @Test
    @DisplayName("Leave workspace - success scenario")
    void leaveWorkspace_Success() throws Exception {
        // Given - Add member first
        dataBuilder.addUserToWorkspaceWithRole(testWorkspace, otherUser, "Member");

        // When & Then
        mockMvc.perform(delete("/workspaces/{id}/leave", testWorkspace.getId())
                .with(user(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(otherUser.getId()));

        // Verify member was removed from database
        assertThat(workspaceMemberRepository.findByWorkspaceIdAndUserId(testWorkspace.getId(), otherUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Access without authentication - unauthorized")
    void accessWithoutAuthentication_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/workspaces/{id}", testWorkspace.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new WorkspaceCreate("Test", "Test"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get workspace boards - success scenario")
    void getWorkspaceBoards_Success() throws Exception {
        // Given - Create a board in the workspace
        dataBuilder.createTestBoardWithWorkspace("Test Board", "Test Description", testUser, testWorkspace);

        // When & Then
        mockMvc.perform(get("/workspaces/{id}/boards", testWorkspace.getId())
                .with(user(testUser))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
