package com.kyut.ordo.feature.board.controller;

import com.kyut.ordo.feature.board.dto.BoardCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberCreate;
import com.kyut.ordo.feature.board.dto.BoardMemberRead;
import com.kyut.ordo.feature.board.dto.BoardMemberUpdate;
import com.kyut.ordo.feature.board.dto.BoardRead;
import com.kyut.ordo.feature.board.dto.BoardRoleCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleRead;
import com.kyut.ordo.feature.board.dto.BoardRoleUpdate;
import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
import com.kyut.ordo.feature.board.service.BoardService;
import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.service.ListService;
import com.kyut.ordo.feature.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final BoardPermissionService boardPermissionService;
    private final ListService listService;

    @GetMapping
    public ResponseEntity<Page<BoardRead>> findAllAccessibleBoards(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<BoardRead> boards = boardService.findAllAccessibleBoards(user, pageable);
        return ResponseEntity.ok(boards);
    }

    @PostMapping
    public ResponseEntity<BoardRead> createBoard(
            @AuthenticationPrincipal UserEntity user,
            BoardCreate dto)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.createBoard(user, dto);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.findById(user, id);
        return ResponseEntity.ok(board);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BoardRead> deleteBoard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.deleteBoard(user, id);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardRead> updateBoard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            BoardCreate dto)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.updateBoard(user, id, dto);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{id}/lists/page")
    public ResponseEntity<Page<ListRead>> findAllTaskListsByBoardId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        Page<ListRead> taskLists = listService.findAllByBoard(user, id, pageable);
        return ResponseEntity.ok(taskLists);
    }

    @GetMapping("/{id}/lists")
    public ResponseEntity<List<ListRead>> findAllTaskListsByBoardId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        List<ListRead> taskLists = listService.findAllByBoard(user, id);
        return ResponseEntity.ok(taskLists);
    }

    @PutMapping("/{id}/members/{newUserId}/{roleId}")
    public ResponseEntity<BoardRead> addMember(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @PathVariable Long newUserId,
            @PathVariable Long roleId)
            throws InsufficientBoardPermissionsException {
        boardService.addMember(user, id, newUserId, roleId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/members/{memberId}/{newRoleId}")
    public ResponseEntity<BoardRead> updateMemberRole(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @PathVariable Long memberId,
            @PathVariable Long newRoleId)
            throws InsufficientBoardPermissionsException {
        boardService.updateMemberRole(user, id, memberId, newRoleId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/roles")
    public ResponseEntity<Page<BoardRoleRead>> findRolesByBoardId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.findRolesByBoardId(user, id, pageable));
    }
    
    @PostMapping("/{id}/roles")
    public ResponseEntity<BoardRoleRead> createRole(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody BoardRoleCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        dto.setBoardId(id);
        return ResponseEntity.ok(boardService.createRole(user, dto));
    }
    
    @PutMapping("/{id}/roles/{roleId}")
    public ResponseEntity<BoardRoleRead> updateRole(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @PathVariable Long roleId,
            @RequestBody BoardRoleUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.updateRole(user, roleId, dto));
    }
    
    @GetMapping("/{id}/my-role")
    public ResponseEntity<BoardRoleRead> getMyRole(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.getMyRole(user, id));
    }
    
    @GetMapping("/{id}/members")
    public ResponseEntity<Page<BoardMemberRead>> findMembersByBoardId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.findMembersByBoardId(user, id, pageable));
    }
    
    @PostMapping("/{id}/members")
    public ResponseEntity<BoardMemberRead> createMember(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody BoardMemberCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.createMember(user, id, dto));
    }
    
    @PutMapping("/{id}/members/{memberId}")
    public ResponseEntity<BoardMemberRead> updateMember(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestBody BoardMemberUpdate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.updateMember(user, id, memberId, dto));
    }
    
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<BoardMemberRead> deleteMember(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @PathVariable Long memberId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        return ResponseEntity.ok(boardService.deleteMember(user, id, memberId));
    }
}
