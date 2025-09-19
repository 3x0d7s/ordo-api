package com.kyut.ordo.feature.workspace;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceInviteEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.feature.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceInviteMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.feature.workspace.repository.WorkspaceInviteRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.feature.workspace.service.WorkspaceInviteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WorkspaceInviteService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceInviteService Unit Tests")
class WorkspaceInviteServiceTest {

    @Mock
    private WorkspaceInviteRepository inviteRepository;
    
    @Mock
    private WorkspaceRepository workspaceRepository;
    
    @Mock
    private WorkspaceRoleRepository roleRepository;
    
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    
    @Mock
    private WorkspaceInviteMapper inviteMapper;
    
    @Mock
    private WorkspaceMapper workspaceMapper;
    
    @InjectMocks
    private WorkspaceInviteService workspaceInviteService;
    
    private UserEntity testUser;
    private UserEntity invitedUser;
    private WorkspaceEntity testWorkspace;
    private WorkspaceRoleEntity ownerRole;
    private WorkspaceRoleEntity memberRole;
    private WorkspaceMemberEntity testMember;
    private WorkspaceInviteEntity testInvite;
    private WorkspaceInviteCreate inviteCreateDto;
    private WorkspaceInviteRead inviteReadDto;
    private WorkspaceRead workspaceReadDto;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        testUser.setId(1L);
        
        invitedUser = TestConfig.TestDataFactory.createTestUser("invited@example.com", "Invited User");
        invitedUser.setId(2L);
        
        testWorkspace = new WorkspaceEntity();
        testWorkspace.setId(1L);
        testWorkspace.setTitle("Test Workspace");
        testWorkspace.setDescription("Test Description");
        testWorkspace.setOwner(testUser);
        
        ownerRole = new WorkspaceRoleEntity();
        ownerRole.setId(1L);
        ownerRole.setName("Owner");
        ownerRole.setAbleToManageSettings(true);
        
        memberRole = new WorkspaceRoleEntity();
        memberRole.setId(2L);
        memberRole.setName("Member");
        memberRole.setAbleToManageSettings(false);
        memberRole.setAbleToManageContent(true);
        
        testMember = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(testUser)
                .role(ownerRole)
                .build();
        
        testInvite = WorkspaceInviteEntity.builder()
                .id(1L)
                .workspace(testWorkspace)
                .createdBy(testUser)
                .token("test-token-123")
                .role(memberRole)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        inviteCreateDto = new WorkspaceInviteCreate();
        inviteCreateDto.setWorkspaceId(1L);
        inviteCreateDto.setRoleId(2L);
        inviteCreateDto.setExpiresInDays(7);
        
        inviteReadDto = new WorkspaceInviteRead();
        inviteReadDto.setId(1L);
        inviteReadDto.setToken("test-token-123");
        
        workspaceReadDto = new WorkspaceRead();
        workspaceReadDto.setId(1L);
        workspaceReadDto.setTitle("Test Workspace");
    }

    @Test
    @DisplayName("Create invite - success scenario")
    void createInvite_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(inviteRepository.save(any(WorkspaceInviteEntity.class))).thenReturn(testInvite);
        when(inviteMapper.toDto(testInvite)).thenReturn(inviteReadDto);

        // When
        WorkspaceInviteRead result = workspaceInviteService.createInvite(testUser, inviteCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test-token-123");
        
        // Verify that an invite entity was saved with proper token generation
        ArgumentCaptor<WorkspaceInviteEntity> inviteCaptor = ArgumentCaptor.forClass(WorkspaceInviteEntity.class);
        verify(inviteRepository).save(inviteCaptor.capture());
        
        WorkspaceInviteEntity savedInvite = inviteCaptor.getValue();
        assertThat(savedInvite.getWorkspace()).isEqualTo(testWorkspace);
        assertThat(savedInvite.getCreatedBy()).isEqualTo(testUser);
        assertThat(savedInvite.getRole()).isEqualTo(memberRole);
        assertThat(savedInvite.getToken()).isNotNull();
        assertThat(savedInvite.getToken()).hasSize(32); // RandomStringUtils.randomAlphanumeric(32)
        assertThat(savedInvite.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
        
        verify(workspaceRepository).findById(1L);
        verify(roleRepository).findById(2L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
    }

    @Test
    @DisplayName("Create invite - workspace not found")
    void createInvite_WorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.createInvite(testUser, inviteCreateDto))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    @DisplayName("Create invite - role not found")
    void createInvite_RoleNotFound() {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.createInvite(testUser, inviteCreateDto))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessageContaining("WorkspaceRole not found");
    }

    @Test
    @DisplayName("Create invite - user not member of workspace")
    void createInvite_UserNotMember() {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.createInvite(testUser, inviteCreateDto))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessageContaining("WorkspaceMember not found");
    }

    @Test
    @DisplayName("Create invite - insufficient permissions")
    void createInvite_InsufficientPermissions() {
        // Given
        WorkspaceMemberEntity memberWithoutPermissions = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(testUser)
                .role(memberRole) // Member role doesn't have manage settings permission
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser))
                .thenReturn(Optional.of(memberWithoutPermissions));

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.createInvite(testUser, inviteCreateDto))
                .isInstanceOf(WorkspaceRoleInsuficientRightsExceptions.class)
                .hasMessageContaining("You don't have permission to delete this workspace");
    }

    @Test
    @DisplayName("Accept invite - success scenario")
    void acceptInvite_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(inviteRepository.findByToken("test-token-123")).thenReturn(Optional.of(testInvite));
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        WorkspaceRead result = workspaceInviteService.acceptInvite(invitedUser, "test-token-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Workspace");
        
        // Verify that a member was created
        ArgumentCaptor<WorkspaceMemberEntity> memberCaptor = ArgumentCaptor.forClass(WorkspaceMemberEntity.class);
        verify(workspaceMemberRepository).save(memberCaptor.capture());
        
        WorkspaceMemberEntity savedMember = memberCaptor.getValue();
        assertThat(savedMember.getWorkspace()).isEqualTo(testWorkspace);
        assertThat(savedMember.getUser()).isEqualTo(invitedUser);
        assertThat(savedMember.getRole()).isEqualTo(memberRole);
        
        // Verify that the invite was deleted
        verify(inviteRepository).delete(testInvite);
        
        verify(inviteRepository).findByToken("test-token-123");
        verify(workspaceMapper).toDto(testWorkspace);
    }

    @Test
    @DisplayName("Accept invite - invite not found")
    void acceptInvite_InviteNotFound() {
        // Given
        when(inviteRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.acceptInvite(invitedUser, "invalid-token"))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessageContaining("Invalid or expired invite");
    }

    @Test
    @DisplayName("Accept invite - invite expired")
    void acceptInvite_InviteExpired() {
        // Given
        WorkspaceInviteEntity expiredInvite = WorkspaceInviteEntity.builder()
                .id(1L)
                .workspace(testWorkspace)
                .createdBy(testUser)
                .token("expired-token")
                .role(memberRole)
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                .build();
        
        when(inviteRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredInvite));

        // When & Then
        assertThatThrownBy(() -> workspaceInviteService.acceptInvite(invitedUser, "expired-token"))
                .isInstanceOf(WorkspaceRoleInsuficientRightsExceptions.class)
                .hasMessageContaining("Invite has expired");
    }

    @Test
    @DisplayName("Token generation is unique and secure")
    void tokenGeneration_IsUniqueAndSecure() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(inviteRepository.save(any(WorkspaceInviteEntity.class))).thenReturn(testInvite);
        when(inviteMapper.toDto(any(WorkspaceInviteEntity.class))).thenReturn(inviteReadDto);

        // When - Create multiple invites
        workspaceInviteService.createInvite(testUser, inviteCreateDto);
        workspaceInviteService.createInvite(testUser, inviteCreateDto);

        // Then - Verify tokens are different
        ArgumentCaptor<WorkspaceInviteEntity> inviteCaptor = ArgumentCaptor.forClass(WorkspaceInviteEntity.class);
        verify(inviteRepository, org.mockito.Mockito.times(2)).save(inviteCaptor.capture());
        
        var savedInvites = inviteCaptor.getAllValues();
        String token1 = savedInvites.get(0).getToken();
        String token2 = savedInvites.get(1).getToken();
        
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).hasSize(32);
        assertThat(token2).hasSize(32);
        assertThat(token1).matches("^[a-zA-Z0-9]+$"); // Alphanumeric only
        assertThat(token2).matches("^[a-zA-Z0-9]+$"); // Alphanumeric only
    }

    @Test
    @DisplayName("Invite expiration calculation")
    void inviteExpirationCalculation() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceInviteCreate customInviteDto = new WorkspaceInviteCreate();
        customInviteDto.setWorkspaceId(1L);
        customInviteDto.setRoleId(2L);
        customInviteDto.setExpiresInDays(14); // Custom 14 days
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(inviteRepository.save(any(WorkspaceInviteEntity.class))).thenReturn(testInvite);
        when(inviteMapper.toDto(any(WorkspaceInviteEntity.class))).thenReturn(inviteReadDto);

        LocalDateTime beforeCall = LocalDateTime.now();

        // When
        workspaceInviteService.createInvite(testUser, customInviteDto);

        LocalDateTime afterCall = LocalDateTime.now();

        // Then
        ArgumentCaptor<WorkspaceInviteEntity> inviteCaptor = ArgumentCaptor.forClass(WorkspaceInviteEntity.class);
        verify(inviteRepository).save(inviteCaptor.capture());
        
        WorkspaceInviteEntity savedInvite = inviteCaptor.getValue();
        LocalDateTime expectedMin = beforeCall.plusDays(14);
        LocalDateTime expectedMax = afterCall.plusDays(14);
        
        assertThat(savedInvite.getExpiresAt()).isBetween(expectedMin, expectedMax);
    }
}
