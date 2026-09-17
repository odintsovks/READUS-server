package com.readus.forum.controller;

import com.readus.forum.dto.BranchResponse;
import com.readus.forum.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAll() {
        return ResponseEntity.ok(branchService.getAll());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<BranchResponse> getByIdOrSlug(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(branchService.getByIdOrSlug(idOrSlug));
    }
}
