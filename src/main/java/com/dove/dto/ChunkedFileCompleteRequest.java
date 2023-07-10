package com.dove.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public record ChunkedFileCompleteRequest(
        @NotBlank
        String uploadId,
        @NotNull
        List<String> eTagList
) {
}
