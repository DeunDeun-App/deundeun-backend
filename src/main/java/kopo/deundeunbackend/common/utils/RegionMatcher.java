package kopo.deundeunbackend.common.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class RegionMatcher {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern DISTRICT = Pattern.compile(".+[시군구]$");

    private static final Map<String, Set<String>> PROVINCES = Map.ofEntries(
            Map.entry("서울특별시", Set.of("서울")),
            Map.entry("부산광역시", Set.of("부산")),
            Map.entry("대구광역시", Set.of("대구")),
            Map.entry("인천광역시", Set.of("인천")),
            Map.entry("광주광역시", Set.of("광주")),
            Map.entry("대전광역시", Set.of("대전")),
            Map.entry("울산광역시", Set.of("울산")),
            Map.entry("세종특별자치시", Set.of("세종")),
            Map.entry("경기도", Set.of("경기")),
            Map.entry("강원도", Set.of("강원")),
            Map.entry("강원특별자치도", Set.of("강원")),
            Map.entry("충청북도", Set.of("충북")),
            Map.entry("충청남도", Set.of("충남")),
            Map.entry("전라북도", Set.of("전북")),
            Map.entry("전북특별자치도", Set.of("전북")),
            Map.entry("전라남도", Set.of("전남")),
            Map.entry("전남광주통합특별시", Set.of("전남", "광주")),
            Map.entry("경상북도", Set.of("경북")),
            Map.entry("경상남도", Set.of("경남")),
            Map.entry("제주도", Set.of("제주")),
            Map.entry("제주특별자치도", Set.of("제주")));

    private RegionMatcher() {
    }

    public static boolean matches(String targetRegions, String userRegion) {
        if (userRegion == null || userRegion.isBlank()) {
            return true;
        }
        if (targetRegions == null || targetRegions.isBlank()) {
            return false;
        }

        Region user = Region.parse(userRegion);
        return Arrays.stream(targetRegions.split(","))
                .map(Region::parse)
                .anyMatch(target -> target.covers(user));
    }

    private record Region(Set<String> provinces, String city) {

        static Region parse(String text) {
            List<String> tokens = Arrays.stream(WHITESPACE.split(text.trim()))
                    .filter(token -> !token.isEmpty())
                    .toList();
            if (tokens.isEmpty()) {
                return new Region(Set.of(), null);
            }
            String first = tokens.get(0);
            String city = tokens.stream()
                    .skip(1)
                    .filter(token -> DISTRICT.matcher(token).matches())
                    .findFirst()
                    .orElse(null);
            return new Region(PROVINCES.getOrDefault(first, Set.of(first)), city);
        }

        boolean covers(Region user) {
            if (provinces.isEmpty() || user.provinces.stream().noneMatch(provinces::contains)) {
                return false;
            }
            return city == null || city.equals(user.city);
        }
    }
}
