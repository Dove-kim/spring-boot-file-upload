package com.dove.controller;

import com.dove.util.AwsS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/files/chunked-files")
@RestController
@RequiredArgsConstructor
public class ChunkedFileController {
    private final AwsS3 awsS3;
    private final ChunkedFileUploadService chunkedFileUploadService;

    @PostMapping("/upload-key")
    public V2Response getChunkedFileUploadKey(
            @Validated
            @RequestBody ChunkedFileUploadStartRequest chunkedFileUploadStartRequest) {

        Long fileChunkedId = chunkedFileUploadService.generateChunkedFileUploadKey(
                chunkedFileUploadStartRequest.fileUploadType(),
                chunkedFileUploadStartRequest.fileExt()
        );

        return V2Response.builder()
                .result("SUCCESS")
                .data(new ChunkedFileUploadKeyResponse(fileChunkedId))
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public V2Response uploadFile(
            @Validated
            @ModelAttribute
            ChunkedFileUploadRequest chunkedFileUploadRequest
    ) throws MissingServletRequestParameterException {
        if (chunkedFileUploadRequest.file() == null || chunkedFileUploadRequest.file().isEmpty()) {
            throw new MissingServletRequestParameterException(
                    chunkedFileUploadRequest.file().getName(),
                    chunkedFileUploadRequest.file().getContentType()
            );
        }

        chunkedFileUploadService.uploadChunkedFile(
                chunkedFileUploadRequest.fileChunkedId(),
                chunkedFileUploadRequest.chunkSeq(),
                chunkedFileUploadRequest.file()
        );

        return V2Response.builder()
                .result("SUCCESS")
                .build();
    }

    @PostMapping("/complete-file-upload")
    public V2Response completeFileUpload(@RequestBody ChunkedFileCompleteRequest chunkedFileCompleteRequest) {
        String fileUrl = chunkedFileUploadService.mergeChunkedFile(chunkedFileCompleteRequest.fileChunkedId());

        return V2Response.builder()
                .result("SUCCESS")
                .data(new ChunkedFileCompleteResponse(fileUrl))
                .build();
    }

    @GetMapping("/testtest")
    public String cleanUp() {
        awsS3.cleanUp();
        return "OK";
    }
}
