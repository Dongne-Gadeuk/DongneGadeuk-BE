package org.example.dongnegadeuk.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GooglePlacesUtil {

    @Value("${google.places.api-key}")
    private String apiKey;

    private static final String FIND_URL  = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
    private static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL) // photo endpoint가 302 리다이렉트
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 너비 800px, 최대 3장 */
    public List<byte[]> fetchStorePhotos(String storeName, String address) {
        return fetchStorePhotos(storeName, address, 800, 3);
    }

    /** 가게명+주소로 검색해 첫 장소의 사진을 최대 maxCount장 바이트로 반환. 없으면 빈 리스트 */
    public List<byte[]> fetchStorePhotos(String storeName, String address, int maxWidth, int maxCount) {
        try {
            List<String> refs = findPhotoReferences(storeName + " " + address, maxCount);
            List<byte[]> images = new ArrayList<>();
            for (String ref : refs) {
                try {
                    images.add(downloadPhoto(ref, maxWidth));
                } catch (Exception e) {
                    log.warn("사진 다운로드 실패, 스킵: {}", e.getMessage());
                }
            }
            return images; // 없으면 빈 리스트
        } catch (Exception e) {
            log.error("구글 장소 사진 조회 실패 ({} {})", storeName, address, e);
            return List.of();
        }
    }

    private List<String> findPhotoReferences(String query, int maxCount) throws Exception {
        String url = FIND_URL
                + "?input=" + enc(query)
                + "&inputtype=textquery"
                + "&fields=photos,place_id,name"
                + "&language=ko"
                + "&key=" + apiKey;

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(res.body());
        if (!"OK".equals(root.path("status").asText())) return List.of();

        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) return List.of();

        JsonNode photos = candidates.get(0).path("photos");
        List<String> refs = new ArrayList<>();
        if (photos.isArray()) {
            for (int i = 0; i < photos.size() && refs.size() < maxCount; i++) {
                String ref = photos.get(i).path("photo_reference").asText(null);
                if (ref != null) refs.add(ref);
            }
        }
        return refs;
    }

    private byte[] downloadPhoto(String photoRef, int maxWidth) throws Exception {
        String url = PHOTO_URL
                + "?maxwidth=" + maxWidth
                + "&photo_reference=" + enc(photoRef)
                + "&key=" + apiKey;

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<byte[]> res = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() != 200) {
            log.error("구글 사진 다운로드 오류 ({})", res.statusCode());
            throw new IllegalStateException("Google photo error: " + res.statusCode());
        }
        return res.body();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
