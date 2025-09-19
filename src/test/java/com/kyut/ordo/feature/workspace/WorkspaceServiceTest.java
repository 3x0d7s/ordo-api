package com.kyut.ordo.feature.workspace;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.feature.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMemberMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceRoleMapper;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.feature.workspace.service.WorkspaceRoleFactory;
import com.kyut.ordo.feature.workspace.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WorkspaceService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceService Unit Tests")
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    
    @Mock
    private WorkspaceRoleRepository workspaceRoleRepository;
    
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    
    @Mock
    private WorkspaceRoleFactory workspaceRoleFactory;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private WorkspaceRoleMapper workspaceRoleMapper;
    
    @Mock
    private WorkspaceMemberMapper workspaceMemberMapper;
    
    @Mock
    private WorkspaceMapper workspaceMapper;
    
    @InjectMocks
    private WorkspaceService workspaceService;
    
    private UserEntity testUser;
    private UserEntity otherUser;
    private WorkspaceEntity testWorkspace;
    private WorkspaceMemberEntity testMember;
    private WorkspaceRoleEntity ownerRole;
    private WorkspaceRoleEntity memberRole;
    private WorkspaceCreate workspaceCreateDto;
    private WorkspaceRead workspaceReadDto;
    private WorkspaceMemberCreate memberCreateDto;
    private WorkspaceMemberRead memberReadDto;
    private WorkspaceRoleCreate roleCreateDto;
    private WorkspaceRoleRead roleReadDto;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        testUser.setId(1L);
        
        otherUser = TestConfig.TestDataFactory.createTestUser("other@example.com", "Other User");
        otherUser.setId(2L);
        
        testWorkspace = new WorkspaceEntity();
        testWorkspace.setId(1L);
        testWorkspace.setTitle("Test Workspace");
        testWorkspace.setDescription("Test Description");
        testWorkspace.setOwner(testUser);
        
        ownerRole = new WorkspaceRoleEntity();
        ownerRole.setId(1L);
        ownerRole.setName("Owner");
        ownerRole.setAbleToManageSettings(true);
        ownerRole.setAbleToManageMembers(true);
        ownerRole.setAbleToManageContent(true);
        ownerRole.setAbleToManageRoles(true);
        
        memberRole = new WorkspaceRoleEntity();
        memberRole.setId(2L);
        memberRole.setName("Member");
        memberRole.setAbleToManageSettings(false);
        memberRole.setAbleToManageMembers(false);
        memberRole.setAbleToManageContent(true);
        memberRole.setAbleToManageRoles(false);
        
        testMember = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(testUser)
                .role(ownerRole)
                .build();
        
        workspaceCreateDto = new WorkspaceCreate("Test Workspace", "Test Description");
        
        workspaceReadDto = new WorkspaceRead();
        workspaceReadDto.setId(1L);
        workspaceReadDto.setTitle("Test Workspace");
        workspaceReadDto.setDescription("Test Description");
        
        memberCreateDto = new WorkspaceMemberCreate();
        memberCreateDto.setUserId(2L);
        memberCreateDto.setWorkspaceId(1L);
        memberCreateDto.setWorkspaceRoleId(2L);
        
        memberReadDto = new WorkspaceMemberRead();
        memberReadDto.setId(1L);
        
        roleCreateDto = new WorkspaceRoleCreate();
        roleCreateDto.setWorkspaceId(1L);
        roleCreateDto.setName("Custom Role");
        roleCreateDto.setAbleToManageContent(true);
        roleCreateDto.setAbleToManageMembers(false);
        roleCreateDto.setAbleToManageSettings(false);
        roleCreateDto.setAbleToManageRoles(false);
        
        roleReadDto = new WorkspaceRoleRead();
        roleReadDto.setId(3L);
        roleReadDto.setName("Custom Role");
    }

    @Test
    @DisplayName("Create workspace - success scenario")
    void createWorkspace_Success() {
        // Given
        Map<String, WorkspaceRoleEntity> rolesMap = Map.of("Owner", ownerRole);
        
        when(workspaceMapper.toEntity(workspaceCreateDto)).thenReturn(testWorkspace);
        when(workspaceRepository.save(testWorkspace)).thenReturn(testWorkspace);
        when(workspaceRoleFactory.rolesAsMap(testWorkspace)).thenReturn(rolesMap);
        when(workspaceMemberRepository.save(any(WorkspaceMemberEntity.class))).thenReturn(testMember);
        when(workspaceMapper.toDto(any(WorkspaceEntity.class), any(Collection.class))).thenReturn(workspaceReadDto);

        // When
        WorkspaceRead result = workspaceService.createWorkspace(testUser, workspaceCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Workspace");
        
        verify(workspaceMapper).toEntity(workspaceCreateDto);
        verify(workspaceRepository).save(testWorkspace);
        verify(workspaceRoleFactory).rolesAsMap(testWorkspace);
        verify(workspaceMemberRepository).save(any(WorkspaceMemberEntity.class));
    }

    @Test
    @DisplayName("Find workspace by ID - success scenario")
    void findById_Success() throws WorkspaceNotFoundException {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        WorkspaceRead result = workspaceService.findById(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Workspace");
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMapper).toDto(testWorkspace);
    }

    @Test
    @DisplayName("Find workspace by ID - workspace not found")
    void findById_WorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workspaceService.findById(testUser, 1L))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessageContaining("Workspace not found by this id");
    }

    @Test
    @DisplayName("Update workspace - success scenario")
    void updateWorkspace_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceUpdate updateDto = new WorkspaceUpdate();
        updateDto.setTitle("Updated Workspace");
        updateDto.setDescription("Updated Description");
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceRepository.save(testWorkspace)).thenReturn(testWorkspace);
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        WorkspaceRead result = workspaceService.updateWorkspace(testUser, 1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceMapper).updateEntityFromDto(updateDto, testWorkspace);
        verify(workspaceRepository).save(testWorkspace);
    }

    @Test
    @DisplayName("Update workspace - insufficient permissions")
    void updateWorkspace_InsufficientPermissions() {
        // Given
        WorkspaceUpdate updateDto = new WorkspaceUpdate();
        WorkspaceMemberEntity memberWithoutPermissions = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(testUser)
                .role(memberRole) // Member role doesn't have manage settings permission
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser))
                .thenReturn(Optional.of(memberWithoutPermissions));

        // When & Then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(testUser, 1L, updateDto))
                .isInstanceOf(WorkspaceRoleInsuficientRightsExceptions.class)
                .hasMessageContaining("You don't have permission to update this workspace");
    }

    @Test
    @DisplayName("Delete workspace - success scenario")
    void deleteWorkspace_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        WorkspaceRead result = workspaceService.deleteWorkspace(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceRepository).delete(testWorkspace);
    }

    @Test
    @DisplayName("Create workspace member - success scenario")
    void createMember_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceRoleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(workspaceMemberRepository.save(any(WorkspaceMemberEntity.class))).thenReturn(testMember);
        when(workspaceMemberMapper.toDto(any(WorkspaceMemberEntity.class))).thenReturn(memberReadDto);

        // When
        WorkspaceMemberRead result = workspaceService.createMember(testUser, 1L, memberCreateDto);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceRoleRepository).findById(2L);
        verify(userRepository).findById(2L);
        verify(workspaceMemberRepository).save(any(WorkspaceMemberEntity.class));
    }

    @Test
    @DisplayName("Create workspace member - member already exists")
    void createMember_MemberAlreadyExists() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceMemberEntity existingMember = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(otherUser)
                .role(memberRole)
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceRoleRepository.findById(2L)).thenReturn(Optional.of(memberRole));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(existingMember));
        when(workspaceMemberRepository.save(existingMember)).thenReturn(existingMember);
        when(workspaceMemberMapper.toDto(existingMember)).thenReturn(memberReadDto);

        // When
        WorkspaceMemberRead result = workspaceService.createMember(testUser, 1L, memberCreateDto);

        // Then
        assertThat(result).isNotNull();
        
        // Verify that existing member's role was updated
        verify(workspaceMemberRepository).save(existingMember);
        assertThat(existingMember.getRole()).isEqualTo(memberRole);
    }

    @Test
    @DisplayName("Create workspace role - success scenario")
    void createRole_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceRoleEntity newRole = new WorkspaceRoleEntity();
        newRole.setName("Custom Role");
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceRoleMapper.toEntity(roleCreateDto)).thenReturn(newRole);
        when(workspaceRoleRepository.save(newRole)).thenReturn(newRole);
        when(workspaceRoleMapper.toDto(newRole)).thenReturn(roleReadDto);

        // When
        WorkspaceRoleRead result = workspaceService.createRole(testUser, roleCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Custom Role");
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceRoleMapper).toEntity(roleCreateDto);
        verify(workspaceRoleRepository).save(newRole);
    }

    @Test
    @DisplayName("Get my role - success scenario")
    void getMyRole_Success() throws WorkspaceNotFoundException {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceRoleMapper.toDto(ownerRole)).thenReturn(roleReadDto);

        // When
        WorkspaceRoleRead result = workspaceService.getMyRole(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceRoleMapper).toDto(ownerRole);
    }

    @Test
    @DisplayName("Find workspaces by owner - success scenario")
    void findAllByOwner_Success() {
        // Given
        Page<WorkspaceEntity> workspacePage = new PageImpl<>(List.of(testWorkspace));
        Page<WorkspaceRead> expectedPage = new PageImpl<>(List.of(workspaceReadDto));
        
        when(workspaceRepository.findAllByOwner(testUser, Pageable.unpaged())).thenReturn(workspacePage);
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        Page<WorkspaceRead> result = workspaceService.findAllByOwner(testUser, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Workspace");
        
        verify(workspaceRepository).findAllByOwner(testUser, Pageable.unpaged());
    }

    @Test
    @DisplayName("Find workspaces by member - success scenario")
    void findAllByMember_Success() {
        // Given
        Page<WorkspaceEntity> workspacePage = new PageImpl<>(List.of(testWorkspace));
        
        when(workspaceRepository.findAllByMembersUser(testUser, Pageable.unpaged())).thenReturn(workspacePage);
        when(workspaceMapper.toDto(testWorkspace)).thenReturn(workspaceReadDto);

        // When
        Page<WorkspaceRead> result = workspaceService.findAllByMember(testUser, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(workspaceRepository).findAllByMembersUser(testUser, Pageable.unpaged());
    }

    @Test
    @DisplayName("Update workspace member role - success scenario")
    void updateMember_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceMemberEntity memberToUpdate = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(otherUser)
                .role(memberRole)
                .build();
        
        WorkspaceMemberUpdate updateDto = new WorkspaceMemberUpdate();
        updateDto.setWorkspaceRoleId(1L); // Change to owner role
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(memberToUpdate));
        when(workspaceRoleRepository.findById(1L)).thenReturn(Optional.of(ownerRole));
        when(workspaceMemberRepository.save(memberToUpdate)).thenReturn(memberToUpdate);
        when(workspaceMemberMapper.toDto(memberToUpdate)).thenReturn(memberReadDto);

        // When
        WorkspaceMemberRead result = workspaceService.updateMember(testUser, 1L, 2L, updateDto);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceMemberRepository).findByWorkspaceIdAndUserId(1L, 2L);
        verify(workspaceRoleRepository).findById(1L);
        verify(workspaceMemberRepository).save(memberToUpdate);
        
        // Verify role was updated
        assertThat(memberToUpdate.getRole()).isEqualTo(ownerRole);
    }

    @Test
    @DisplayName("Delete workspace member - success scenario")
    void deleteMember_Success() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        WorkspaceMemberEntity memberToDelete = WorkspaceMemberEntity.builder()
                .workspace(testWorkspace)
                .user(otherUser)
                .role(memberRole)
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 2L)).thenReturn(Optional.of(memberToDelete));
        when(workspaceMemberMapper.toDto(memberToDelete)).thenReturn(memberReadDto);

        // When
        WorkspaceMemberRead result = workspaceService.deleteMember(testUser, 1L, 2L);

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceRepository).findById(1L);
        verify(workspaceMemberRepository).findByWorkspaceAndUser(testWorkspace, testUser);
        verify(workspaceMemberRepository).findByWorkspaceIdAndUserId(1L, 2L);
        verify(workspaceMemberRepository).delete(memberToDelete);
    }

    @Test
    @DisplayName("Leave workspace - user can leave their own membership")
    void deleteMember_UserLeavesOwnMembership() throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        // Given
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceAndUser(testWorkspace, testUser)).thenReturn(Optional.of(testMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 1L)).thenReturn(Optional.of(testMember));
        when(workspaceMemberMapper.toDto(testMember)).thenReturn(memberReadDto);

        // When
        WorkspaceMemberRead result = workspaceService.deleteMember(testUser, 1L, 1L); // Same user ID

        // Then
        assertThat(result).isNotNull();
        
        verify(workspaceMemberRepository).delete(testMember);
    }
}
