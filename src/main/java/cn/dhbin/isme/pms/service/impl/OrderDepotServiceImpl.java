package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BadRequestException;
import cn.dhbin.isme.pms.domain.dto.ProfileDto;
import cn.dhbin.isme.pms.domain.entity.OrderDepot;
import cn.dhbin.isme.pms.mapper.OrderDepotMapper;
import cn.dhbin.isme.pms.service.OrderDepotService;
import cn.dhbin.isme.pms.service.OrderService;
import cn.dhbin.isme.pms.service.ProfileService;
import cn.dhbin.isme.pms.util.Constants;
import cn.hutool.core.convert.NumberWithFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class OrderDepotServiceImpl extends ServiceImpl<OrderDepotMapper, OrderDepot> implements OrderDepotService {
    private final OrderService orderService;
    private final ProfileService profileService;

    // 接单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void wareReceiveOrder(Long orderId) {
        // 1. 先判断这个单子有没有被接
        LambdaQueryWrapper<OrderDepot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDepot::getOrderId, orderId);
        Long count = getBaseMapper().selectCount(queryWrapper);
        if (count == 0) {
            // 2. 再去接单
            NumberWithFormat wareIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
            OrderDepot orderDepot = new OrderDepot();
            // 仓库id
            orderDepot.setWareId(wareIdFormat.longValue());
            // 查一下仓库的地址
            ProfileDto profileDto = profileService.findByUserId(wareIdFormat.longValue()).convert(ProfileDto.class);
            String address = profileDto.getAddress();
            String name = profileDto.getNickName();
            String phone = profileDto.getPhone();
            // 塞入信息
            orderDepot.setWarePhone(phone);
            orderDepot.setWareNickName(name);
            orderDepot.setWareAddress(address);
            // 塞入订单编号
            orderDepot.setOrderId(orderId);

            this.save(orderDepot);
            // 修改订单的状态
            orderService.modifyOrderStatus(orderId, Constants.ORDER_WARE_ACP);
        } else {
            throw new BadRequestException("该订单已被接单");
        }
    }

    // 出库
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void wareOutOrder(Long orderId) {
        // 出库，用这个更新一下update_time的时间
        OrderDepot orderDepot = new OrderDepot();
        orderDepot.setUpdateTime(LocalDateTime.now());
        // 手动更新时间
        this.update(orderDepot, new LambdaQueryWrapper<OrderDepot>().eq(OrderDepot::getOrderId, orderId));
        // 修改订单的状态
        orderService.modifyOrderStatus(orderId, Constants.ORDER_OUT_WARE);
    }


}
