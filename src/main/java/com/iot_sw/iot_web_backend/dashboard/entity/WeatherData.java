package com.iot_sw.iot_web_backend.dashboard.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_weather_location_code", columnList = "location_code"),
                @Index(name = "idx_weather_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_code")
    private Integer locationCode;

    @Column(name = "temp_ta")
    private Double tempTa;

    @Column(name = "wind_dir_wd")
    private Double windDirWd;

    @Column(name = "wind_speed_ws")
    private Double windSpeedWs;

    @Column(name = "humidity_hm")
    private Double humidityHm;

    @Column(name = "precipitation_rn")
    private Double precipitationRn;

    @Column(name = "is_strong_wind_warning")
    private Byte isStrongWindWarning;

    @Column(name = "is_dry_warning")
    private Byte isDryWarning;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
