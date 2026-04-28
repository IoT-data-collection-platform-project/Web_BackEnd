CREATE TABLE sensor_telemetry (
                                  mac_address VARCHAR(17) NOT NULL COMMENT '기기 MAC 주소',
                                  measured_at DATETIME(3) NOT NULL COMMENT '센서 측정 시간 (밀리초 단위)',

                                  temperature DECIMAL(5,2) COMMENT '온도',
                                  humidity DECIMAL(5,2) COMMENT '습도',
                                  pressure DECIMAL(6,2) COMMENT '기압 (hPa)',
                                  tvoc SMALLINT UNSIGNED COMMENT '총 휘발성 유기화합물',
                                  eco2 SMALLINT UNSIGNED COMMENT '이산화탄소 환산값',
                                  flame_value SMALLINT COMMENT '화염 센서 아날로그 값',

                                  PRIMARY KEY (mac_address, measured_at)
)
-- measured_at 기준으로 파티셔닝
    PARTITION BY RANGE COLUMNS(measured_at) (
    PARTITION p_max VALUES LESS THAN (MAXVALUE) -- 예외데이터를 위한 보험
);