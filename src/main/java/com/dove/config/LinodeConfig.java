package com.dove.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LinodeConfig {
    private final LinodeProperties linodeProperties;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCredentials =
                new BasicAWSCredentials(linodeProperties.getCredentials().accessKey(), linodeProperties.getCredentials().secretKey());
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder
                                .EndpointConfiguration("https://" + linodeProperties.getRegion() + ".linodeobjects.com", linodeProperties.getRegion())
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(awsCredentials)
                )
                .enablePathStyleAccess()
                .disableChunkedEncoding()
                .build();

        return amazonS3;
    }
}
