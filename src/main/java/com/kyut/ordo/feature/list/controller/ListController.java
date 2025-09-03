package com.kyut.ordo.feature.list.controller;

import com.kyut.ordo.feature.card.dto.CardRead;
import com.kyut.ordo.feature.list.dto.ListPositionUpdate;
import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.card.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.list.dto.ListCreate;
import com.kyut.ordo.feature.list.exception.ListNotFoundException;
import com.kyut.ordo.feature.list.service.ListService;
import com.kyut.ordo.feature.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class ListController {
    private final ListService listService;
    private final CardService cardService;

    @GetMapping("/{id}")
    public ResponseEntity<ListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = listService.findById(user, id);
        return ResponseEntity.ok(taskList);
    }

    @GetMapping("/{id}/cards")
    public ResponseEntity<Page<CardRead>> findAllByListPaged(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        Page<CardRead> tasks = cardService.findAllByList(user, id, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @PostMapping
    public ResponseEntity<ListRead> createTaskList(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody ListCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = listService.createTaskList(user, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ListRead> updateTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody ListCreate dto)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = listService.updateTaskList(user, id, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ListRead> deleteTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = listService.deleteList(user, id);
        return ResponseEntity.ok(taskList);
    }

    @PutMapping("/positions")
    public ResponseEntity<Void> updateListPositions(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody ListPositionUpdate positionUpdate)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        listService.updateListPositions(user, positionUpdate.getBoardId(), positionUpdate.getListIds());
        return ResponseEntity.ok().build();
    }
}
