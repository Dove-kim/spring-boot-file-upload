package com.dove.dto;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public record ChunkedFileUploadRequest(
        @NotBlank
        String uploadKey,
        @NotNull
        @Positive
        Integer chunkSeq,
        @NotNull
        MultipartFile file
) {
}
