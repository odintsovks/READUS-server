package com.readus.forum.repository;

import com.readus.forum.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, java.util.UUID> {
    Optional<Branch> findBySlug(String slug);

    List<Branch> findAllByOrderByCreatedAtAsc();
}
