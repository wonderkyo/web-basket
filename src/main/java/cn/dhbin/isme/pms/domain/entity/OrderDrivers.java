package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_drivers")
public class OrderDrivers implements Convert {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "order_id")
    private Long orderId;

    @TableField(value = "driver_id")
    private Long driverId;

    @TableField(value = "driver_name")
    private String driverName;

    @TableField(value = "car_id")
    private String carId;

    @TableField(value = "driver_phone")
    private String driverPhone;
    // 接单时间
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 拿到货了，开始运输时间
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    // 送货到达时间
    @TableField(value = "arrive_time")
    private LocalDateTime arriveTime;

    @TableField(value = "img_list")
    private String imgList;
}
