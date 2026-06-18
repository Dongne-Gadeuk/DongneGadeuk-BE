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
import java.util.Optional;

@Slf4j
@Service
public class KakaoMapUtil {

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    private static final String ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    private static final String KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 주소를 좌표로 변환한 뒤, 그 근처에서 가게명으로 검색해
     * 일치하는 장소가 있으면 반환한다. (없으면 Optional.empty)
     */
    public Optional<KakaoPlace> findNearbyStore(String address, String storeName) {
        try {
            // 1) 주소 -> 좌표
            Coordinate coord = geocode(address);
            if (coord == null) {
                log.warn("주소 좌표 변환 실패: {}", address);
                return Optional.empty();
            }

            // 2) 좌표 근처에서 가게명 키워드 검색 (반경 500m)
            return searchKeywordNearby(storeName, coord, 500);
        } catch (Exception e) {
            log.error("카카오 지도 검색 실패 (addr={}, name={})", address, storeName, e);
            return Optional.empty();
        }
    }

    /** 주소 -> 좌표 (x=경도, y=위도) */
    private Coordinate geocode(String address) throws Exception {
        JsonNode root = get(ADDRESS_URL + "?query=" + enc(address));
        JsonNode docs = root.path("documents");
        if (!docs.isArray() || docs.isEmpty()) return null;

        JsonNode first = docs.get(0);
        return new Coordinate(first.path("x").asDouble(), first.path("y").asDouble());
    }

    /** 좌표 근처에서 키워드(가게명)로 장소 검색, 이름이 맞으면 반환 */
    private Optional<KakaoPlace> searchKeywordNearby(String keyword, Coordinate c, int radiusMeters) throws Exception {
        String url = KEYWORD_URL
                + "?query=" + enc(keyword)
                + "&x=" + c.x()
                + "&y=" + c.y()
                + "&radius=" + radiusMeters
                + "&sort=distance";

        JsonNode docs = get(url).path("documents");
        if (!docs.isArray() || docs.isEmpty()) return Optional.empty();

        String target = normalize(keyword);
        for (JsonNode d : docs) {
            String placeName = d.path("place_name").asText();
            String n = normalize(placeName);
            // 띄어쓰기/지점명 차이를 감안해 양방향 포함 검사
            if (n.contains(target) || target.contains(n)) {
                return Optional.of(new KakaoPlace(
                        placeName,
                        d.path("category_name").asText(null),
                        d.path("category_group_code").asText(null),
                        d.path("category_group_name").asText(null),
                        d.path("road_address_name").asText(null),
                        d.path("address_name").asText(null),
                        d.path("x").asDouble(),
                        d.path("y").asDouble()
                ));
            }
        }
        return Optional.empty();
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "KakaoAK " + restApiKey) // ← Bearer 아님 주의
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            log.error("카카오 API 오류 ({}): {}", res.statusCode(), res.body());
            throw new IllegalStateException("Kakao API error: " + res.statusCode());
        }
        return objectMapper.readTree(res.body());
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** 띄어쓰기/대소문자 차이를 줄이기 위한 정규화 */
    private static String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "").toLowerCase();
    }

    public record Coordinate(double x, double y) {}   // x=경도(lng), y=위도(lat)
    public record KakaoPlace(
            String name,
            String categoryName,       // "음식점 > 카페 > 커피전문점"
            String categoryGroupCode,  // "CE7"
            String categoryGroupName,  // "카페"
            String roadAddress,
            String address,
            double x,
            double y
    ) {}
}