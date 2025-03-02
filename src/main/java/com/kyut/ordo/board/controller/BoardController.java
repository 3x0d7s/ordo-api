package com.kyut.ordo.board.controller;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.board.service.BoardService;
import com.kyut.ordo.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final BoardPermissionService boardPermissionService;

    @GetMapping
    public ResponseEntity<Page<BoardRead>> findAllAccessibleBoards(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<BoardRead> boards = boardService.findAllAccessibleBoards(user, pageable);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.findById(user, id);
        return ResponseEntity.ok(board);
    }

    @PostMapping
    public ResponseEntity<BoardRead> createBoard(
            @AuthenticationPrincipal UserEntity user,
            BoardCreate dto)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.createBoard(user, dto);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<BoardRead> deleteBoard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id)
            throws InsufficientBoardPermissionsException {
        BoardRead board = boardService.deleteBoard(user, id);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{boardId}/members/{newUserId}/{roleId}")
    public ResponseEntity<BoardRead> addMember(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long boardId,
            @PathVariable Long newUserId,
            @PathVariable Long roleId)
            throws InsufficientBoardPermissionsException {
        boardService.addMember(user, boardId, newUserId, roleId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{boardId}/members/{memberId}/{newRoleId}")
    public ResponseEntity<BoardRead> updateMemberRole(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long boardId,
            @PathVariable Long memberId,
            @PathVariable Long newRoleId)
            throws InsufficientBoardPermissionsException {
        boardService.updateMemberRole(user, boardId, memberId, newRoleId);
        return ResponseEntity.ok().build();
    }




}
