CREATE TABLE sensor_telemetry (
                                  mac_address VARCHAR(17) NOT NULL COMMENT '기기 MAC 주소',
                                  measured_at DATETIME(3) NOT NULL COMMENT '센서 측정 시간 (밀리초 단위)',

    -- 데이터 타입 최적화 (공간 절약 = Insert 성능 향상)
                                  temperature DECIMAL(5,2) COMMENT '온도',
                                  humidity DECIMAL(5,2) COMMENT '습도',
                                  pressure DECIMAL(6,2) COMMENT '기압 (hPa)',
                                  tvoc SMALLINT UNSIGNED COMMENT '총 휘발성 유기화합물',
                                  eco2 SMALLINT UNSIGNED COMMENT '이산화탄소 환산값',
                                  flame_value SMALLINT COMMENT '화염 센서 아날로그 값',

    -- 파티셔닝을 위한 복합 기본 키 설정 (순서 매우 중요!)
                                  PRIMARY KEY (mac_address, measured_at)
)
-- 파티셔닝 전략: 측정 시간(measured_at) 기준으로 일별(Daily) 분할
    PARTITION BY RANGE COLUMNS(measured_at) (
    PARTITION p_max VALUES LESS THAN (MAXVALUE) -- 미래 데이터를 위한 안전장치
);