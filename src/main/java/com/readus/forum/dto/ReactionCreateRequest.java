package com.readus.forum.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionCreateRequest {
    /** 1 = Like, 2 = Dislike, 3 = Helpful. */
    @NotNull
    @Min(1)
    @Max(3)
    private Short type;
}
