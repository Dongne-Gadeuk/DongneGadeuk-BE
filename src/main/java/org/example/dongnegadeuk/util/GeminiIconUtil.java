package org.example.dongnegadeuk.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiIconUtil {

    @Value("${gemini.icon-key}")
    private String apiKey;

    @Value("${removebg.api-key}")
    private String removeBgApiKey;

    private static final String IMAGE_MODEL = "gemini-2.5-flash-image";
    private static final String TEXT_MODEL = "gemini-2.5-flash";
    private static final String IMAGE_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + IMAGE_MODEL + ":generateContent";
    private static final String TEXT_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + TEXT_MODEL + ":generateContent";
    private static final String REMOVEBG_ENDPOINT = "https://api.remove.bg/v1.0/removebg";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record GeneratedIcon(byte[] image, String name) {}

    public GeneratedIcon generateIcon(String storeName, String category, List<byte[]> photos) {
        try {
            byte[] image = generateImage(storeName, category, photos);
            String name = generateName(storeName, category);
            return new GeneratedIcon(image, cleanName(name));
        } catch (Exception e) {
            log.error("제미나이 아이콘 생성 실패 ({}, {})", storeName, category, e);
            throw new RuntimeException("아이콘 생성 실패", e);
        }
    }

    private byte[] generateImage(String storeName, String category, List<byte[]> photos) throws Exception {
        List<Map<String, Object>> parts = new ArrayList<>();

        boolean hasPhoto = photos != null && !photos.isEmpty();
        if (hasPhoto) {
            for (byte[] p : photos) {
                parts.add(Map.of("inline_data", Map.of(
                        "mime_type", "image/jpeg",
                        "data", Base64.getEncoder().encodeToString(p)
                )));
            }
        }
        parts.add(Map.of("text", buildImagePrompt(storeName, category, hasPhoto)));

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", parts)),
                "generationConfig", Map.of(
                        "responseModalities", List.of("IMAGE")
                )
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(IMAGE_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            log.error("제미나이 이미지 생성 오류 ({}): {}", res.statusCode(), res.body());
            throw new IllegalStateException("Gemini image gen error: " + res.statusCode());
        }

        JsonNode respParts = objectMapper.readTree(res.body())
                .path("candidates").get(0).path("content").path("parts");

        for (JsonNode part : respParts) {
            JsonNode inline = part.has("inlineData") ? part.path("inlineData") : part.path("inline_data");
            if (!inline.isMissingNode() && inline.has("data")) {
                byte[] rawImage = Base64.getDecoder().decode(inline.path("data").asText());
                return removeBackground(rawImage);
            }
        }
        throw new IllegalStateException("응답에 이미지가 없음: " + res.body());
    }

    private byte[] removeBackground(byte[] imageBytes) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "image_file_b64", base64Image,
                "size", "auto"
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(REMOVEBG_ENDPOINT))
                .header("X-Api-Key", removeBgApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<byte[]> res = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());

        if (res.statusCode() != 200) {
            log.warn("배경 제거 실패 ({}), 원본 사용", res.statusCode());
            return imageBytes;
        }

        log.info("배경 제거 성공, 결과 크기: {} bytes", res.body().length);
        return res.body();
    }

    private String generateName(String storeName, String category) throws Exception {
        String namePrompt = """
                가게명: %s
                카테고리: %s

                이 가게 분위기에 어울리는 수집용 가구/소품 이름을 한국어로 딱 하나만 지어줘.
                "수식구 + 가구명칭" 형식으로. 예: 빈티지 에스프레소 머신, 아늑한 원목 의자
                반드시 20글자 이내로. 공백 포함 20자를 넘으면 안 돼.
                이름 한 줄만 출력하고 다른 설명은 하지 마.
                """.formatted(storeName, category);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", namePrompt))
                ))
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(TEXT_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            log.warn("이름 생성 실패 ({}): {}", res.statusCode(), res.body());
            return "이름 없는 소품";
        }

        return objectMapper.readTree(res.body())
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText("이름 없는 소품");
    }

    private String buildImagePrompt(String storeName, String category, boolean hasPhoto) {
        String vibeSource = hasPhoto
                ? "첨부한 가게 사진들에서 분위기, 주요 색감, 재질, 세련된 정도를 읽고,"
                : "가게명과 카테고리만으로 분위기와 어울리는 색을 추론해서,";

        return """
                %s 그 느낌에 어울리는 수집용 가구/소품 아이콘을 딱 하나 만들어줘.

                가게명: %s
                카테고리: %s

                이미지 스타일:
                - Isometric 2D game asset of a cute object, vector art style
                - Thick clean outlines, cute cell-shaded illustration, simple flat colors with minimal shading
                - Cozy and whimsical mobile game aesthetic
                - No photorealism, no 3D rendering, no glossy reflections
                - The object must be large and fill most of the canvas, centered
                - White or solid light background is fine — background will be removed in post-processing
                - The object should be the only element
                - Do NOT include any text, letters, or labels in the image
                """.formatted(vibeSource, storeName, category);
    }

    private String cleanName(String raw) {
        if (raw == null || raw.isBlank()) return "이름 없는 소품";
        String name = raw.strip().split("\\r?\\n")[0].strip();
        name = name.replaceAll("^[\"'`*\\s]+|[\"'`*\\s]+$", "");
        return name.isBlank() ? "이름 없는 소품" : name;
    }
}