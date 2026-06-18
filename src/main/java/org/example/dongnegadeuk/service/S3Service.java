package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.config.AppProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AppProperties props;

    private static final String ITEM_PREFIX = "items/";   // root-prefix(images) 말고 items 폴더

    /** 아이콘 업로드 후 영구 공개 URL 반환 */
    public String uploadIcon(byte[] bytes) {
        String key = ITEM_PREFIX + UUID.randomUUID() + ".png";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(props.getS3().getBucket())
                        .key(key)
                        .contentType("image/png")
                        .build(),
                RequestBody.fromBytes(bytes)
        );

        return toPublicUrl(key);
    }

    private String toPublicUrl(String key) {
        String cf = props.getS3().getCloudfrontDomain();
        if (cf != null && !cf.isBlank()) {
            return "https://" + cf + "/" + key;     // CloudFront 쓰면 그쪽
        }
        return s3Client.utilities()
                .getUrl(b -> b.bucket(props.getS3().getBucket()).key(key))
                .toString();                        // 표준 S3 공개 URL
    }
}