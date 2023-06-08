package com.dove.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkedFileService {
    private final AmazonS3Client amazonS3Client;

    public InitiateMultipartUploadResult initiateUploadChunkedFile() {
        //S3 분할 파일 업로드 init
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3Client.initiateMultipartUpload(
                new InitiateMultipartUploadRequest("버킷 명", "업로드 위치"));

        //필요할 경우 DB에 분할 파일 업로드 정보 저장.

        return initiateMultipartUploadResult;
    }


    /**
     * 분할 파일을 업로드한다.
     *
     */
    public UploadPartResult uploadChunkedFile(String uploadId, Integer chunkSeq, MultipartFile multipartFile) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        UploadPartRequest uploadPartRequest = new UploadPartRequest()
                .withBucketName("버킷 명")
                .withKey("업로드 위치")
                .withUploadId(uploadId)//initiateMultipartUpload를 통해 받은 id
                .withPartNumber(chunkSeq) // 순차적인 part number (1~10,000)
                .withInputStream(multipartFile.getInputStream())
                .withObjectMetadata(objectMetadata)
                .withPartSize(multipartFile.getSize()); // chunk file size

        return amazonS3Client.uploadPart(uploadPartRequest);
    }

    /**
     * 업로드 한 분할 파일을 하나의 파일로 합친다.
     */
    public CompleteMultipartUploadResult mergeChunkedFile(String uploadId, List<PartETag> eTagList) {
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                "버킷 명",
                "업로드 위치",
                uploadId,
                eTagList);//분할 파일을 업로드 하는 과정에서 받은 eTag들의 list

        return amazonS3Client.completeMultipartUpload(completeRequest);
    }
}
