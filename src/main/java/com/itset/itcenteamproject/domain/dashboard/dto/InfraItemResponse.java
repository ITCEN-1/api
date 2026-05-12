package com.itset.itcenteamproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//요소별 세부정보(이름/좌표) 1건
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfraItemResponse {
    private String name;
    private Double latitude;
    private Double longitude;
    private String line;
}
