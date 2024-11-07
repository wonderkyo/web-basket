package cn.dhbin.isme.pms.domain.dto;
import cn.dhbin.mapstruct.helper.core.Convert;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderDetailDto implements Convert {

    private Long orderId;// order表
    private String status;//订单状态
    private String productName;//货名，order表
    private int productQuant;//数量，order表
    private LocalDateTime createTime;//下单时间，order表
    private LocalDateTime downTime;// 签收时间，order表里面
    private String recName;// 收件人姓名
    private String recPhone;// 收件人手机号
    private String address;// 收件人手机号

    private Long wareId;//接单的仓库，order表
    private LocalDateTime wareAcpTime;//仓库接单时间，order表
    private LocalDateTime wareOutTime;// 仓库出库时间（货物正在等待揽收）,order表
    private String warePhone; // 仓库的联系方式
    private String wareNickName; // 仓库的联系人姓名
    private String wareAddress; // 仓库地址

    private String carId;// 车牌号，放在了profile里面的，order_drivers表，插入的时候放在user表里面
    private String driverName;//司机名称，order_drivers表
    private String driverPhone;//司机的联系方式
    private LocalDateTime driverAcpTime;// 司机接单时间
    private LocalDateTime driverStartTime;// 司机取到货的时间，开始运输，order_drivers表
    private LocalDateTime driverArriveTime;// 送达时间，司机：xxx（车牌号：xxx，联系电话：xxx），派送中，order_drivers表
    private String imgList; // 图片存放地址

    private String basketList; // 筐子的列表

}
