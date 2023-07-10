package com.dove.controller;

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.dove.dto.ChunkedFileCompleteRequest;
import com.dove.dto.ChunkedFileUploadRequest;
import com.dove.service.ChunkedFileService;
import com.dove.util.AwsS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequestMapping("/files/chunked-files")
@RestController
@RequiredArgsConstructor
public class ChunkedFileController {
    private final AwsS3 awsS3;
    private final ChunkedFileService chunkedFileService;

    @PostMapping("/upload-key")
    public ResponseEntity getChunkedFileUploadKey() {

        //업로드 키 생성
        InitiateMultipartUploadResult initiateMultipartUploadResult = chunkedFileService.initiateUploadChunkedFile();

        return ResponseEntity.ok(initiateMultipartUploadResult.getUploadId());
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity uploadFile(
            @Validated
            @ModelAttribute
            ChunkedFileUploadRequest chunkedFileUploadRequest
    ) throws IOException {

        UploadPartResult uploadPartResult = chunkedFileService.uploadChunkedFile(
                chunkedFileUploadRequest.uploadKey(),
                chunkedFileUploadRequest.chunkSeq(),
                chunkedFileUploadRequest.file()
        );

        return ResponseEntity.ok(uploadPartResult.getPartETag().getETag());
    }

    @PostMapping("/complete-file-upload")
    public ResponseEntity completeFileUpload(
            @RequestBody
            ChunkedFileCompleteRequest chunkedFileCompleteRequest) {
        chunkedFileService.mergeChunkedFile(chunkedFileCompleteRequest.uploadId(), chunkedFileCompleteRequest.eTagList());

        return ResponseEntity.ok("SUCCESS");
    }

    @GetMapping("/clean")
    public String cleanUp() {
        awsS3.cleanUp();
        return "OK";
    }
}
