package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_depot")
public class OrderDepot implements Convert {
    @TableId(value="id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "ware_id")
    private Long wareId;

    @TableField(value = "order_id")
    private Long orderId;

    @TableField(value="create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value="update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "ware_address")
    private String wareAddress;

    @TableField(value = "ware_phone")
    private String warePhone;

    @TableField(value = "ware_nickname")
    private String wareNickName;


}
