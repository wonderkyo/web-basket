package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order implements Convert {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @TableField(value = "product_name")
    private String productName;

    @TableField(value = "product_quant")
    private int productQuant;

    @TableField(value = "store_id")
    private Long storeId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "down_time")
    private LocalDateTime downTime;

    private String address;

    //收件人姓名
    @TableField(value = "rec_name")
    private String recName;

    // 收件人手机号
    @TableField(value = "rec_phone")
    private String recPhone;

    // 状态
//    create-刚创建完
//    wareAcp-仓库已接单，待揽件
//    driverAcp-司机接单
//    outWare-司机来了之后，仓库已出库
//    driverBegin-司机开始运输（运输中）
//    driveArrive-司机已到达，待门店收货
//    storeAcp-门店已收货
    private String status;

}
