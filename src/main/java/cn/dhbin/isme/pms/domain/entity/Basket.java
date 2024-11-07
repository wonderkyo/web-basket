package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("basket")
public class Basket implements Convert {
    @TableId("basket_rfid")
    private String basketRfid;

    @TableField("user_id")
    private Long userId;

    // 默认状态为1,1-空闲；2-占用,3-损坏,4-归还在途,5-丢失（智能是管理员或者商店弄）
    private String status;

    @TableField(value="create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value="update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("order_id")
    private Long orderId;

}
