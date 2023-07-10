package com.dove.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.dove.config.LinodeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChunkedFileService {
    //업로드 할 위치
    private final String S3_KEY = "chunked/test";

    private final LinodeProperties linodeProperties;

    private final AmazonS3Client amazonS3Client;

    public InitiateMultipartUploadResult initiateUploadChunkedFile() {
        //S3 분할 파일 업로드 init
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3Client.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(linodeProperties.getBucket(), S3_KEY));

        return initiateMultipartUploadResult;
    }


    /**
     * 분할 파일을 업로드한다.
     *
     */
    public UploadPartResult uploadChunkedFile(String uploadKey, Integer chunkSeq, MultipartFile multipartFile) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        UploadPartRequest uploadPartRequest = new UploadPartRequest()
                .withBucketName(linodeProperties.getBucket())
                .withKey(S3_KEY)
                .withUploadId(uploadKey)//initiateMultipartUpload를 통해 받은 id
                .withPartNumber(chunkSeq) // 순차적인 part number (1~10,000)
                .withInputStream(multipartFile.getInputStream())
                .withObjectMetadata(objectMetadata)
                .withPartSize(multipartFile.getSize()); // chunk file size

        return amazonS3Client.uploadPart(uploadPartRequest);
    }

    /**
     * 업로드 한 분할 파일을 하나의 파일로 합친다.
     */
    public CompleteMultipartUploadResult mergeChunkedFile(String uploadId, List<String> eTagList) {

        List<PartETag> partETagList = new ArrayList<>();

        for (int i = 0; i < eTagList.size(); i++) {
            partETagList.add(new PartETag(i+1, eTagList.get(i)));
        }

        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                linodeProperties.getBucket(),
                S3_KEY,
                uploadId,
                partETagList);//분할 파일을 업로드 하는 과정에서 받은 eTag들의 list

        return amazonS3Client.completeMultipartUpload(completeRequest);
    }
}
