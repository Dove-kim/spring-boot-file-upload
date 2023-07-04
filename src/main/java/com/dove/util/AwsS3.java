package com.dove.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.dove.config.LinodeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3 {
    private final LinodeProperties linodeProperties;

    private final AmazonS3 linodeClient;

    /**
     * 단일 파일을 가져온다.
     *
     * @param key
     * @return
     */
    public S3Object getObject(String key) {
        return linodeClient.getObject(linodeProperties.getBucket(), key);
    }

    /**
     * 단일 버킷에 있는 모든 object을 조회한다.
     * object는 파일, 디렉토리를 모두 포함한다.
     *
     * @return
     */
    public List<S3ObjectSummary> getAllObjectSummary() {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(linodeProperties.getBucket());

        List<S3ObjectSummary> returnData = new ArrayList<>();

        ListObjectsV2Result result = null;
        do {
            result = linodeClient.listObjectsV2(listObjectsV2Request);

            if (result.isTruncated()) {
                listObjectsV2Request.setContinuationToken(result.getNextContinuationToken());
            }

            returnData.addAll(result.getObjectSummaries());
        } while (result.isTruncated());

        return returnData;
    }


    /**
     * <h1>파일 업로드</h1>
     *
     * <p>단일 MultipartFile을 받아서 업로드한다.</p>
     *
     * @param key  s3 key
     * @param file MultipartFile포맷 파일
     * @throws Exception
     */
    public void uploadFile(String key, MultipartFile file) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        linodeClient.putObject(
                new PutObjectRequest(linodeProperties.getBucket(), key, file.getInputStream(), objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicReadWrite)
        );

    }

    /**
     * <h1>다중 파일 제거</h1>
     *
     * @param keyList s3 Key 리스트
     */
    public void deleteByKeyList(List<String> keyList) {
        if (keyList.size() > 0) {
            for (String key : keyList) {
                deleteByKey(key);
            }

        }
    }

    /**
     * <h1>단일 파일 제거</h1>
     *
     * @param key s3 key
     */
    public void deleteByKey(String key) {
        try {
            linodeClient.deleteObject(linodeProperties.getBucket(), key);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * S3에 multipart 분할 업로드를 위한 1단계 요청
     * <p>
     * https://docs.aws.amazon.com/AmazonS3/latest/userguide/mpuoverview.html
     *
     * @param key
     * @return
     */
    public InitiateMultipartUploadResult multipartUploadInitiation(String key) {
        return linodeClient.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(linodeProperties.getBucket(), key));
    }

    /**
     * S3에 multipart 분할 업로드를 위한 2단계 요청
     *
     * @param key
     * @param uploadId
     * @param chunkedSeq  분할 된 파일 순번
     * @param chunkedFile 분할 된 파일
     * @return
     * @throws IOException
     */
    public PartETag uploadChunkedFile(String key, String uploadId, Integer chunkedSeq, MultipartFile chunkedFile) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(chunkedFile.getContentType());
        objectMetadata.setContentLength(chunkedFile.getSize());

        UploadPartRequest uploadPartRequest = new UploadPartRequest()
                .withBucketName(linodeProperties.getBucket()) // 버킷 이름
                .withKey(key) // s3에 올라갈 파일 경로+이름
                .withUploadId(uploadId) // init에서 받은 upload id
                .withPartNumber(chunkedSeq) // 순차적인 part number (1~10,000)
                .withInputStream(chunkedFile.getInputStream())
                .withObjectMetadata(objectMetadata)
                .withPartSize(chunkedFile.getSize()); // chunk file size

        UploadPartResult uploadPartResult = linodeClient.uploadPart(uploadPartRequest);
        return uploadPartResult.getPartETag();
    }

    /**
     * S3에 multipart 분할 업로드를 위한 마지막 요청, 업로드한 분할 파일 완료 요청.
     *
     * @param key
     * @param uploadId
     * @param partETagList
     */
    public void CompleteChunkedUpload(String key, String uploadId, List<PartETag> partETagList) {
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                linodeProperties.getBucket(),
                key,
                uploadId,
                partETagList);

        linodeClient.completeMultipartUpload(completeRequest);
    }

    public void cleanUp() {
        MultipartUploadListing multipartUploadListing =
                linodeClient.listMultipartUploads(new ListMultipartUploadsRequest("test-autowini-object"));

        List<MultipartUpload> multipartUploads = multipartUploadListing.getMultipartUploads();
        for (MultipartUpload multipartUpload : multipartUploads) {
            log.info("upload id: {}, key: {}", multipartUpload.getUploadId(), multipartUpload.getKey());
            linodeClient.abortMultipartUpload(
                    new AbortMultipartUploadRequest(
                            linodeProperties.getBucket(),
                            multipartUpload.getKey(),
                            multipartUpload.getUploadId()));
        }
    }
}
