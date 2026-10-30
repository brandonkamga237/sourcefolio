package com.brandonkamga.sourcefolio.storage.infra.config;

import com.brandonkamga.sourcefolio.storage.application.MediaStore;
import com.brandonkamga.sourcefolio.storage.application.MediaStoreImpl;
import com.brandonkamga.sourcefolio.storage.domain.port.ObjectStorage;
import com.brandonkamga.sourcefolio.storage.infra.s3.S3ObjectStorage;
import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Configuration
class StorageServices {

    private static final Logger log = LoggerFactory.getLogger(StorageServices.class);

    // Path-style addressing (http://rustfs:9000/bucket/key): RustFS has no per-bucket host names.
    @Bean
    S3Client s3Client(@Value("${app.storage.endpoint}") String endpoint, @Value("${app.storage.region}") String region,
                      @Value("${app.storage.access-key}") String accessKey, @Value("${app.storage.secret-key}") String secretKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5))
                        .socketTimeout(Duration.ofSeconds(30)))
                .build();
    }

    @Bean
    ObjectStorage objectStorage(S3Client s3, @Value("${app.storage.bucket}") String bucket) {
        return new S3ObjectStorage(s3, bucket);
    }

    // Not wrapped by TransactionalServices: it only talks to the bucket.
    @Bean
    MediaStore mediaStore(ObjectStorage storage, @Value("${app.storage.public-url}") String publicUrl) {
        return new MediaStoreImpl(storage, publicUrl);
    }

    // Browsers read previews straight from the bucket (Traefik serves /media), so the bucket is
    // public for reading and nothing else. A failure is logged, not fatal: the site works
    // without previews, only uploads fail.
    @Bean
    @ConditionalOnProperty(name = "app.storage.ensure-bucket", havingValue = "true")
    ApplicationRunner bucketInitializer(S3Client s3, @Value("${app.storage.bucket}") String bucket) {
        return arguments -> {
            try {
                try {
                    s3.headBucket(request -> request.bucket(bucket));
                } catch (NoSuchBucketException missing) {
                    s3.createBucket(request -> request.bucket(bucket));
                    log.info("Created bucket {}", bucket);
                }
                s3.putBucketPolicy(request -> request.bucket(bucket).policy("""
                        {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},
                        "Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}]}""".formatted(bucket)));
            } catch (S3Exception | software.amazon.awssdk.core.exception.SdkClientException failure) {
                log.error("Bucket {} not ready: previews cannot be stored until it is", bucket, failure);
            }
        };
    }
}
