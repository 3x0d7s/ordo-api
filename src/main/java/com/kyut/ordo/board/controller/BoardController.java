package com.kyut.ordo.board.controller;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.board.service.BoardService;
import com.kyut.ordo.list.dto.ListRead;
import com.kyut.ordo.list.service.ListService;
import com.kyut.ordo.user.entity.UserEntity;
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




}
