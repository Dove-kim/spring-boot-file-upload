package com.dove.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.linode")
public class LinodeProperties {
    private String bucket;
    private String region;
    private Credentials credentials;
}
