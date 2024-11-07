package cn.dhbin.isme.pms.domain.dto;

import cn.dhbin.mapstruct.helper.core.Convert;
import lombok.Data;

@Data
public class OrderStatusCountDto implements Convert {
    private Long createCount;
    private Long wareAcpCount;
    private Long driverAcpCount;
    private Long outWareCount;
    private Long driverBeginCount;
    private Long driveArriveCount;
    private Long storeAcpCount;

}
