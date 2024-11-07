package cn.dhbin.isme.pms.domain.entity;
import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("basket_return")
public class BasketReturn implements Convert {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("driver_id")
    private Long driverId;

    @TableField("store_id")
    private Long storeId;

    @TableField("basket_list")
    private String basketList;

    @TableField(value="create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value="update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // status: 1-创建。2-被司机接收了
    private String status;

}
