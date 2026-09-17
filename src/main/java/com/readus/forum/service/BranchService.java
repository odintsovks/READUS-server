package com.readus.forum.service;

import com.readus.forum.dto.BranchResponse;
import com.readus.forum.entity.Branch;
import com.readus.forum.exception.NotFoundException;
import com.readus.forum.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchResponse> getAll() {
        return branchRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public BranchResponse getByIdOrSlug(String idOrSlug) {
        return toResponse(findByIdOrSlug(idOrSlug));
    }

    public Branch findByIdOrSlug(String idOrSlug) {
        UUID id = parseUuid(idOrSlug);
        if (id != null) {
            return branchRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Branch not found: " + idOrSlug));
        }
        return branchRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + idOrSlug));
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .slug(branch.getSlug())
                .description(branch.getDescription())
                .build();
    }
}
