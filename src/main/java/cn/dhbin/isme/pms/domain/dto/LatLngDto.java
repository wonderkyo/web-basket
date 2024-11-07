package cn.dhbin.isme.pms.domain.dto;

import lombok.Data;

@Data
public class LatLngDto {
    private Double lat;
    private Double lng;
    private String locData;
    private Long id;
    private String styleId;
}
