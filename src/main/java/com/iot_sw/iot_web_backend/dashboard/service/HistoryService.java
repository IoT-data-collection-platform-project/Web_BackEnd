package com.iot_sw.iot_web_backend.dashboard.service;

import com.iot_sw.iot_web_backend.dashboard.dto.HistoryPointDto;
import com.iot_sw.iot_web_backend.dashboard.dto.HistoryResponseDto;
import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;
import com.iot_sw.iot_web_backend.dashboard.repository.WeatherRepository;
import com.iot_sw.iot_web_backend.device.entity.SensorTelemetry;
import com.iot_sw.iot_web_backend.device.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final SensorRepository sensorRepository;
    private final WeatherRepository weatherRepository;

    public HistoryResponseDto getHistory(String periodRaw, String macAddressRaw) {
        String period = normalizePeriod(periodRaw);
        String macAddress = normalizeMac(macAddressRaw);
        LocalDate baseDate = resolveBaseDate(macAddress);
        List<HistoryPointDto> points = switch (period) {
            case "weekly" -> getWeekly(baseDate, macAddress);
            case "monthly" -> getMonthly(baseDate, macAddress);
            default -> getDaily(baseDate, macAddress);
        };

        return new HistoryResponseDto(period, baseDate, macAddress, points);
    }

    private List<HistoryPointDto> getDaily(LocalDate baseDate, String macAddress) {
        LocalDateTime dayStart = baseDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<SensorTelemetry> sensors = findSensors(macAddress, dayStart, dayEnd);
        List<WeatherData> weathers = weatherRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(dayStart, dayEnd);

        int[] starts = {0, 4, 8, 12, 16, 20, 23};
        List<HistoryPointDto> points = new ArrayList<>();
        for (int i = 0; i < starts.length; i++) {
            int startHour = starts[i];
            int nextHour = i + 1 < starts.length ? starts[i + 1] : 24;
            LocalDateTime bucketStart = baseDate.atTime(startHour, 0);
            LocalDateTime bucketEnd = nextHour == 24 ? dayEnd : baseDate.atTime(nextHour, 0);
            String label = startHour == 23 ? "23:59" : String.format(Locale.ROOT, "%02d:00", startHour);
            points.add(buildPoint(label, bucketStart, bucketEnd, sensors, weathers));
        }
        return points;
    }

    private List<HistoryPointDto> getWeekly(LocalDate baseDate, String macAddress) {
        LocalDate startDate = baseDate.minusDays(6);
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = baseDate.plusDays(1).atStartOfDay();
        List<SensorTelemetry> sensors = findSensors(macAddress, rangeStart, rangeEnd);
        List<WeatherData> weathers = weatherRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(rangeStart, rangeEnd);

        List<HistoryPointDto> points = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            LocalDateTime bucketStart = day.atStartOfDay();
            LocalDateTime bucketEnd = day.plusDays(1).atStartOfDay();
            String label = String.format(Locale.ROOT, "%02d.%02d", day.getMonthValue(), day.getDayOfMonth());
            points.add(buildPoint(label, bucketStart, bucketEnd, sensors, weathers));
        }
        return points;
    }

    private List<HistoryPointDto> getMonthly(LocalDate baseDate, String macAddress) {
        YearMonth yearMonth = YearMonth.from(baseDate);
        LocalDate monthStartDate = yearMonth.atDay(1);
        LocalDate monthEndDate = yearMonth.atEndOfMonth();
        LocalDateTime rangeStart = monthStartDate.atStartOfDay();
        LocalDateTime rangeEnd = monthEndDate.plusDays(1).atStartOfDay();

        List<SensorTelemetry> sensors = findSensors(macAddress, rangeStart, rangeEnd);
        List<WeatherData> weathers = weatherRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(rangeStart, rangeEnd);

        List<HistoryPointDto> points = new ArrayList<>();
        int endDay = yearMonth.lengthOfMonth();
        for (int day = 1; day <= endDay; day++) {
            LocalDate current = yearMonth.atDay(day);
            LocalDateTime bucketStart = current.atStartOfDay();
            LocalDateTime bucketEnd = current.plusDays(1).atStartOfDay();
            String label = String.format(Locale.ROOT, "%02d", day);
            points.add(buildPoint(label, bucketStart, bucketEnd, sensors, weathers));
        }
        return points;
    }

    private HistoryPointDto buildPoint(
            String label,
            LocalDateTime bucketStart,
            LocalDateTime bucketEnd,
            List<SensorTelemetry> sensors,
            List<WeatherData> weathers
    ) {
        Double indoorTemp = averageBigDecimal(
                sensors.stream()
                        .filter(s -> isBetween(s.getMeasuredAt(), bucketStart, bucketEnd))
                        .map(SensorTelemetry::getTemperature)
                        .toList()
        );
        Double indoorHumidity = averageBigDecimal(
                sensors.stream()
                        .filter(s -> isBetween(s.getMeasuredAt(), bucketStart, bucketEnd))
                        .map(SensorTelemetry::getHumidity)
                        .toList()
        );
        Double outdoorTemp = averageDouble(
                weathers.stream()
                        .filter(w -> isBetween(w.getCreatedAt(), bucketStart, bucketEnd))
                        .map(WeatherData::getTempTa)
                        .toList()
        );

        return new HistoryPointDto(label, bucketStart, bucketEnd, indoorTemp, outdoorTemp, indoorHumidity);
    }

    private boolean isBetween(LocalDateTime target, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return target != null
                && !target.isBefore(startInclusive)
                && target.isBefore(endExclusive);
    }

    private List<SensorTelemetry> findSensors(String macAddress, LocalDateTime start, LocalDateTime end) {
        if (macAddress.isBlank()) {
            return sensorRepository.findByMeasuredAtBetweenOrderByMeasuredAtAsc(start, end);
        }
        return sensorRepository.findByMacAddressAndMeasuredAtBetweenOrderByMeasuredAtAsc(macAddress, start, end);
    }

    private Double averageBigDecimal(List<BigDecimal> values) {
        List<BigDecimal> filtered = values.stream().filter(v -> v != null).toList();
        if (filtered.isEmpty()) return null;
        double sum = filtered.stream().mapToDouble(BigDecimal::doubleValue).sum();
        return sum / filtered.size();
    }

    private Double averageDouble(List<Double> values) {
        List<Double> filtered = values.stream().filter(v -> v != null).toList();
        if (filtered.isEmpty()) return null;
        double sum = filtered.stream().mapToDouble(Double::doubleValue).sum();
        return sum / filtered.size();
    }

    private LocalDate resolveBaseDate(String macAddress) {
        Optional<LocalDateTime> latestSensor = macAddress.isBlank()
                ? sensorRepository.findTopByOrderByMeasuredAtDesc().map(SensorTelemetry::getMeasuredAt)
                : sensorRepository.findTopByMacAddressOrderByMeasuredAtDesc(macAddress).map(SensorTelemetry::getMeasuredAt);
        Optional<LocalDateTime> latestWeather = weatherRepository.findTopByOrderByCreatedAtDesc().map(WeatherData::getCreatedAt);

        LocalDateTime maxDateTime = latestSensor.orElse(LocalDateTime.MIN);
        if (latestWeather.isPresent() && latestWeather.get().isAfter(maxDateTime)) {
            maxDateTime = latestWeather.get();
        }
        if (maxDateTime.equals(LocalDateTime.MIN)) {
            return LocalDate.now();
        }
        return maxDateTime.toLocalDate();
    }

    private String normalizePeriod(String periodRaw) {
        if (periodRaw == null) return "daily";
        String normalized = periodRaw.trim().toLowerCase(Locale.ROOT);
        if ("weekly".equals(normalized) || "monthly".equals(normalized) || "daily".equals(normalized)) {
            return normalized;
        }
        return "daily";
    }

    private String normalizeMac(String macAddressRaw) {
        if (macAddressRaw == null) return "";
        String mac = macAddressRaw.trim();
        if ("00:00:00:00:00:00".equals(mac)) return "";
        return mac;
    }
}
