package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BadRequestException;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.ProfileDto;
import cn.dhbin.isme.pms.domain.dto.QueryOrderDto;
import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.isme.pms.domain.entity.OrderDepot;
import cn.dhbin.isme.pms.domain.entity.OrderDrivers;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import cn.dhbin.isme.pms.mapper.OrderDriversMapper;
import cn.dhbin.isme.pms.service.OrderDriversService;
import cn.dhbin.isme.pms.service.OrderService;
import cn.dhbin.isme.pms.service.ProfileService;
import cn.dhbin.isme.pms.util.Constants;
import cn.hutool.core.convert.NumberWithFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDriversServiceImpl extends ServiceImpl<OrderDriversMapper, OrderDrivers> implements OrderDriversService {

    private final OrderService orderService;
    private final ProfileService profileService;

    // 司机接单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void driverAccept(Long orderId) {
        OrderDrivers orderDrivers = new OrderDrivers();
        NumberWithFormat driverId = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        // 1、判断这个用户有没有待完成的订单，查order_drivers，driverId等于当前id，而且arriveTime等于空
        Long undownOrderCount = getBaseMapper().selectCount(new LambdaQueryWrapper<OrderDrivers>().eq(OrderDrivers::getDriverId, driverId.longValue()).isNull(OrderDrivers::getArriveTime));
        if (undownOrderCount == 0) {
            LambdaQueryWrapper<OrderDrivers> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDrivers::getOrderId, orderId);
            Long count = getBaseMapper().selectCount(queryWrapper);
            if (count == 0) {
                // 司机接收
                orderDrivers.setOrderId(orderId);
                orderDrivers.setDriverId(driverId.longValue());
                // 查profile
                ProfileDto profileDto = profileService.findByUserId(driverId.longValue()).convert(ProfileDto.class);
                String carNo = profileDto.getCarNo();
                String phone = profileDto.getPhone();
                String nickName = profileDto.getNickName();

                orderDrivers.setCarId(carNo);
                orderDrivers.setDriverPhone(phone);
                orderDrivers.setDriverName(nickName);
                this.save(orderDrivers);
                // 修改订单的状态
                orderService.modifyOrderStatus(orderId, Constants.ORDER_DRIVER_ACP);
            } else {
                throw new BadRequestException("该订单已被接单");
            }
        } else {
            throw new BadRequestException("您已接过其他订单，订单送达后才可接新订单");
        }
    }

    @Override
    public OrderDetailDto queryDriversOrder() {
        NumberWithFormat driverId = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        // driverId等于，arriveTime是空的
        OrderDrivers orderDrivers = getBaseMapper().selectOne(
                new LambdaQueryWrapper<OrderDrivers>()
                        .eq(OrderDrivers::getDriverId, driverId.longValue())
                        .isNull(OrderDrivers::getArriveTime)
                        .last("LIMIT 1"));
        if (orderDrivers == null) {
            return null;
        } else {
            Long orderId = orderDrivers.getOrderId();
            // 查一下订单信息
            OrderDetailDto orderDetail = orderService.getOrderDetails(orderId);
            return orderDetail;
        }
    }

    @Override
    public void updateOrderDriverTime(String updateType, Long orderId, String imgList) {
        if (updateType.equals("start")) {
            this.update(null, new UpdateWrapper<OrderDrivers>().set("start_time", LocalDateTime.now()).set("img_list", imgList).eq("order_id", orderId));
            orderService.modifyOrderStatus(orderId, Constants.ORDER_DRIVER_BEGIN);

        } else if (updateType.equals("arrive")) {
            this.update(null, new UpdateWrapper<OrderDrivers>().set("arrive_time", LocalDateTime.now()).eq("order_id", orderId));
            orderService.modifyOrderStatus(orderId, Constants.ORDER_DRIVER_ARRIVE);

        } else {
            throw new BadRequestException("更新类型错误");
        }
    }

    @Override
    public Page<QueryOrderDto> queryDriverHisOrders(Long userId, QueryHisRequest request) {
        MPJLambdaWrapper<OrderDrivers> objectMPJLambdaWrapper = new MPJLambdaWrapper<>();
        IPage<QueryOrderDto> orderParam = request.toPage();
        // 查询所有到达时间不为空的就算是已经到了
        objectMPJLambdaWrapper.selectAll(OrderDrivers.class)
                .selectAs(Order::getStatus, "status")
                .selectAs(Order::getProductName, "productName")
                .selectAs(Order::getProductQuant, "productQuant")
                .selectAs(Order::getDownTime, "downTime")
                .selectAs(Order::getAddress, "address")
                .selectAs(Order::getRecName, "recName")
                .selectAs(Order::getRecPhone, "recPhone")
                .selectAs(OrderDepot::getWareAddress, "wareAddress")
                .leftJoin(Order.class, Order::getOrderId, OrderDrivers::getOrderId)
                .leftJoin(OrderDepot.class, OrderDepot::getOrderId, OrderDrivers::getOrderId)
                .eq(OrderDrivers::getDriverId, userId)
                .isNotNull(OrderDrivers::getArriveTime)
                .orderByDesc(OrderDrivers::getArriveTime);

        orderParam = getBaseMapper().selectJoinPage(
                orderParam,
                QueryOrderDto.class,
                objectMPJLambdaWrapper);

        Page<QueryOrderDto> orderPage = new Page<>();
        orderPage.setPageData(orderParam.getRecords());
        orderPage.setTotal(orderParam.getTotal());
        return orderPage;
    }

    @Override
    public String uploadImg(MultipartFile file, Long orderId) {
        // 上传文件
        log.info("file:{}", file);
        // 确定文件保存的路径图片
        String path = "E:\\wxPics\\";
        // 构造文件的完整路径
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fullPath = path + fileName;
        // 将文件保存到本地
        try {
            file.transferTo(new java.io.File(fullPath));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("图片上传失败");
        }
        // 查询当前的OrderDrivers实体
        OrderDrivers orderDrivers = this.lambdaQuery()
                .eq(OrderDrivers::getOrderId, orderId)
                .one();
        String imgList = orderDrivers.getImgList();
        if (imgList == null || imgList.isEmpty()) {
            orderDrivers.setImgList(fileName);
        } else {
            imgList = imgList + "," + fileName;
            orderDrivers.setImgList(imgList);
        }
        this.update(orderDrivers, new LambdaQueryWrapper<OrderDrivers>().eq(OrderDrivers::getOrderId, orderId));
        return fileName;
    }

}
