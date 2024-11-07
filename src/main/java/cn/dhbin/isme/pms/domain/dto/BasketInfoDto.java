package cn.dhbin.isme.pms.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BasketInfoDto {
    private String basketRfid;
    // 连表查user的loc
    private String basketLoc;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long userId;
    private String role;
}
