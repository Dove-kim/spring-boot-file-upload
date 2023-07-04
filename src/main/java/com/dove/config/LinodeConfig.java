package com.dove.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
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
        AWSCredentials credentials = new BasicAWSCredentials(
                linodeProperties.getCredentials().accessKey(),
                linodeProperties.getCredentials().secretKey()
        );


        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://ap-south-1.linodeobjects.com", linodeProperties.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

//        new AmazonS3Client(credentials,new ClientConfiguration(n));
//        return AmazonS3ClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
////                .withRegion(region)
//                .build();
//        return AmazonS3ClientBuilder
//                .standard()
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
//                        "https://" + linodeProperties.getRegion() + ".linodeobjects.com" + linodeProperties.getBucket(), linodeProperties.getRegion()))
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .build();
    }
}
