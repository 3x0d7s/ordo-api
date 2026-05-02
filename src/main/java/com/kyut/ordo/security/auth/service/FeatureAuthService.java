package com.kyut.ordo.security.auth.service;

import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.board.repository.BoardMemberRepository;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.repository.BoardRoleRepository;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.comment.repository.CommentRepository;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.task.repository.TaskRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureAuthService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRoleRepository boardRoleRepository;
    private final ListRepository listRepository;
    private final CardRepository cardRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final UserRepository userRepository;

    public boolean canAccessBoard(Long boardId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (boardId == null || userId == null) {
            return false;
        }

        return boardRepository.findById(boardId).map(board -> {
            if (boardMemberRepository.findByBoardIdAndUserId(boardId, userId).isPresent()) {
                return true;
            }

            if (board.getVisibility() == BoardVisibility.PUBLIC) {
                return true;
            }

            return board.getVisibility() == BoardVisibility.WORKSPACE
                    && board.getWorkspace() != null
                    && workspaceMemberRepository.findByWorkspaceIdAndUserId(board.getWorkspace().getId(), userId).isPresent();
        }).orElse(false);
    }

    public boolean canEditBoard(Long boardId, Authentication authentication) {
        return hasBoardPermission(boardId, authentication, "EDIT");
    }

    public boolean canDeleteBoard(Long boardId, Authentication authentication) {
        return hasBoardPermission(boardId, authentication, "DELETE");
    }

    public boolean canInviteBoardMembers(Long boardId, Authentication authentication) {
        return hasBoardPermission(boardId, authentication, "INVITE");
    }

    public boolean canManageBoardRoles(Long boardId, Authentication authentication) {
        return hasBoardPermission(boardId, authentication, "MANAGE_ROLES");
    }

    public boolean canCreateLists(Long boardId, Authentication authentication) {
        return hasBoardPermission(boardId, authentication, "CREATE_LISTS");
    }

    public boolean canManageBoardRolesByRoleId(Long roleId, Authentication authentication) {
        if (roleId == null) {
            return false;
        }

        return boardRoleRepository.findById(roleId)
                .map(role -> canManageBoardRoles(role.getBoard().getId(), authentication))
                .orElse(false);
    }

    public boolean canCreateBoard(Long workspaceId, Authentication authentication) {
        if (workspaceId == null) {
            return true;
        }

        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }

        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole().isAbleToManageContent())
                .orElse(false);
    }

    public boolean canViewListCards(Long listId, Authentication authentication) {
        return hasPermissionByListId(listId, authentication, "EDIT");
    }

    public boolean canCreateCard(Long listId, Authentication authentication) {
        return hasPermissionByListId(listId, authentication, "CREATE_TASKS");
    }

    public boolean canAccessCard(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "EDIT");
    }

    public boolean canEditCard(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "EDIT");
    }

    public boolean canEditBoardByList(Long listId, Authentication authentication) {
        return hasPermissionByListId(listId, authentication, "EDIT");
    }

    public boolean canAccessTask(Long taskId, Authentication authentication) {
        return hasPermissionByTaskId(taskId, authentication, "EDIT");
    }

    public boolean canViewCardTasks(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "EDIT");
    }

    public boolean canCreateTask(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "EDIT");
    }

    public boolean canViewCardComments(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "EDIT");
    }

    public boolean canAccessComment(Long commentId, Authentication authentication) {
        if (commentId == null) {
            return false;
        }

        return commentRepository.findById(commentId)
                .map(comment -> hasBoardPermission(comment.getCard().getList().getBoard().getId(), authentication, "EDIT"))
                .orElse(false);
    }

    public boolean canCreateComment(Long cardId, Authentication authentication) {
        return hasPermissionByCardId(cardId, authentication, "CREATE_TASKS");
    }

    public boolean canManageComment(Long commentId, Authentication authentication) {
        if (commentId == null) {
            return false;
        }

        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }

        return commentRepository.findById(commentId).map(comment -> {
            if (comment.getCreatedBy() != null && userId.equals(comment.getCreatedBy().getId())) {
                return true;
            }
            return hasBoardPermission(comment.getCard().getList().getBoard().getId(), authentication, "MANAGE_ROLES");
        }).orElse(false);
    }

    public boolean canManageWorkspaceSettings(Long workspaceId, Authentication authentication) {
        if (workspaceId == null) {
            return false;
        }

        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }

        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole().isAbleToManageSettings())
                .orElse(false);
    }

    public boolean canManageWorkspaceSettingsByRoleId(Long roleId, Authentication authentication) {
        if (roleId == null) {
            return false;
        }

        return workspaceRoleRepository.findById(roleId)
                .map(role -> canManageWorkspaceSettings(role.getWorkspace().getId(), authentication))
                .orElse(false);
    }

    public boolean canManageWorkspaceSettingsOrSelf(Long workspaceId, Long targetUserId, Authentication authentication) {
        if (targetUserId == null) {
            return false;
        }

        Long currentUserId = extractUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        return currentUserId.equals(targetUserId) || canManageWorkspaceSettings(workspaceId, authentication);
    }

    private boolean hasPermissionByListId(Long listId, Authentication authentication, String permission) {
        if (listId == null) {
            return false;
        }

        return listRepository.findById(listId)
                .map(list -> hasBoardPermission(list.getBoard().getId(), authentication, permission))
                .orElse(false);
    }

    private boolean hasPermissionByCardId(Long cardId, Authentication authentication, String permission) {
        if (cardId == null) {
            return false;
        }

        return cardRepository.findById(cardId)
                .map(card -> hasBoardPermission(card.getList().getBoard().getId(), authentication, permission))
                .orElse(false);
    }

    private boolean hasPermissionByTaskId(Long taskId, Authentication authentication, String permission) {
        if (taskId == null) {
            return false;
        }

        return taskRepository.findById(taskId)
                .map(task -> hasBoardPermission(task.getCard().getList().getBoard().getId(), authentication, permission))
                .orElse(false);
    }

    private boolean hasBoardPermission(Long boardId, Authentication authentication, String permission) {
        Long userId = extractUserId(authentication);
        if (boardId == null || userId == null) {
            return false;
        }

        return boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .map(member -> switch (permission) {
                    case "EDIT" -> member.getRole().isAbleToEdit();
                    case "DELETE" -> member.getRole().isAbleToDelete();
                    case "INVITE" -> member.getRole().isAbleToInviteMembers();
                    case "MANAGE_ROLES" -> member.getRole().isAbleToManageRoles();
                    case "CREATE_LISTS" -> member.getRole().isAbleToCreateLists();
                    case "CREATE_TASKS" -> member.getRole().isAbleToCreateTasks();
                    default -> false;
                })
                .orElse(false);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserEntity userEntity) {
            return userEntity.getId();
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(UserEntity::getId)
                    .orElse(null);
        }

        return null;
    }
}
