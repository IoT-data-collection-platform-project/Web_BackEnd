package com.iot_sw.iot_web_backend.dashboard.entity;
import jakarta.persistence.*; 
import lombok.*; 

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tm;     // 시간
    private Double wd;     // 풍향
    private Double ws;     // 풍속
    private Double ta;     // 기온
    private Double hm;     // 습도
    private Double rn;     // 강수량
}
