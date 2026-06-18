package org.example.dongnegadeuk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.dto.ReceiptDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.example.dongnegadeuk.common.exception.errorCode.ReceiptErrorCode.OCR_FAILED;

@Slf4j
@Service
public class ReceiptUpstageVisionService {

    @Value("${upstage.api-key}")
    private String apiKey;

    private static final String ENDPOINT = "https://api.upstage.ai/v1/information-extraction/chat/completions";
    private static final String MODEL = "information-extract";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Gemini 쪽 RESPONSE_SCHEMA와 동일한 필드 구성의 JSON Schema */
    private static final Map<String, Object> RECEIPT_JSON_SCHEMA = Map.of(
            "name", "receipt_schema",
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "businessNumber", Map.of(
                                    "type", "string",
                                    "description", """
                                        사업자등록번호. 숫자와 하이픈(-)만 포함된 값만 추출.
                                        '사업자/고유번호:', '사업자번호:' 등 라벨 뒤에 슬래시(/)로 구분된 경우 슬래시 앞 부분만 사용.
                                        예) '1018126409/6597-3822-8317' → '1018126409'
                                        예) '101-81-26409/6597-3822-8317' → '101-81-26409'
                                        """
                            ),
                            "storeName", Map.of(
                                    "type", "string",
                                    "description", "실제 가게/가맹점 이름. 카드사명(KB국민카드 등)은 제외."
                            ),
                            "storeAddress", Map.of(
                                    "type", "string",
                                    "description", """
                                        가맹점 도로명 주소. 건물번호까지만 포함하고 괄호 안 동/읍/면 등 행정동 정보는 제외.
                                        예) '서울 종로구 대학로 130 (동숭동)' → '서울 종로구 대학로 130'
                                        예) '서울특별시 강남구 테헤란로 123 (역삼동)' → '서울특별시 강남구 테헤란로 123'
                                        """
                            ),
                            "transactionDate", Map.of(
                                    "type", "string",
                                    "description", """
                                        거래 일시. 반드시 YYYY-MM-DD 형식으로만 출력. 시간은 버릴 것.
                                        연도가 2자리면 2000년대로 변환. 예) 26 → 2026.
                                        예) '26/04/19 16:35:38' → '2026-04-19'
                                        예) '2023.07.12 19:33' → '2023-07-12'
                                        """
                            ),
                            "totalAmount", Map.of(
                                    "type", "integer",
                                    "description", """
                                        최종 결제 합계 금액. 금액(공급가액)+부가세=합계 형식이면 합계값 사용.
                                        숫자만, 쉼표/원 표시 없이. 예) 6,200원 → 6200
                                        """
                            )
                    ),
                    "required", List.of("businessNumber", "storeName", "storeAddress", "transactionDate", "totalAmount")
            )
    );

    public ReceiptDto parse(byte[] imageBytes) {
        try {
            byte[] resized = imageBytes;
            String base64Image = Base64.getEncoder().encodeToString(resized);
            String dataUrl = "data:image/jpeg;base64," + base64Image;

            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                                    )
                            )
                    ),
                    "response_format", Map.of(
                            "type", "json_schema",
                            "json_schema", RECEIPT_JSON_SCHEMA
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Upstage API 오류 응답 ({}): {}", response.statusCode(), response.body());
                throw new CustomException(OCR_FAILED);
            }

            JsonNode root = objectMapper.readTree(response.body());
            // OpenAI 호환 chat completions 응답: choices[0].message.content 안에 스키마대로 채워진 JSON 문자열이 들어있음
            String content = root.path("choices").get(0).path("message").path("content").asText();

            JsonNode parsed = objectMapper.readTree(content);

            return new ReceiptDto(
                    textOrNull(parsed, "businessNumber"),
                    textOrNull(parsed, "storeName"),
                    textOrNull(parsed, "storeAddress"),
                    textOrNull(parsed, "transactionDate"),
                    intOrNull(parsed, "totalAmount")
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Upstage 비전 파싱 실패", e);
            throw new CustomException(OCR_FAILED);
        }
    }

    /** 원본이 1568px보다 작으면 업스케일하지 않고 그대로 사용 */
    private byte[] resizeIfNeeded(byte[] original) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
        if (img.getWidth() <= 1568 && img.getHeight() <= 1568) {
            return original;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(img)
                .size(1568, 1568)
                .outputFormat("jpg")
                .outputQuality(0.95)
                .toOutputStream(out);
        return out.toByteArray();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String digits = v.asText().replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : Integer.parseInt(digits);
    }
}