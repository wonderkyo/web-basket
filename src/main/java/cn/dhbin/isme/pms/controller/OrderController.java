package cn.dhbin.isme.pms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.dto.*;
import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.isme.pms.domain.request.CreateOrderRequest;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import cn.dhbin.isme.pms.service.OrderService;
import cn.hutool.core.convert.NumberWithFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订单")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/createOrder")
    @SaCheckRole("store")
    public R<Void> createOrder(@RequestBody @Validated CreateOrderRequest request){
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        if (userIdFormat.longValue() != request.getStoreId()) {
            throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能本人下单！");
        }
        orderService.createOrder(request);
        return R.ok();
    }

//    @PostMapping("/queryHisOrder")
//    @SaCheckRole(value = {"driver", "store", "depot", "admin"}, mode = SaMode.OR)
//    public R<Page<HisOrdersDto>> queryHisOrder(@RequestBody @Validated QueryHisRequest request) {
//        NumberWithFormat userId =
//                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
//        Page<HisOrdersDto> hisOrders = orderService.queryHisOrders(userId.longValue(), request);
//        return R.ok(hisOrders);
//    }

    @GetMapping("/getOrderDetails")
    public R<OrderDetailDto> getOrderDetails(@RequestParam Long orderId) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        OrderDetailDto orderDetail = orderService.getOrderDetails(orderId);
        return R.ok(orderDetail);
    }

    @PostMapping("/queryOnlyOrders")
    public R<Page<Order>> queryOnlyOrdersPage(@RequestBody @Validated QueryHisRequest request) {
        Page<Order> unWareOrders = orderService.queryOnlyOrdersPage(request);
        return R.ok(unWareOrders);
    }

    // 根据id查order表
    @GetMapping("/queryOrderById")
    public R<Order> queryOrderById(@RequestParam Long orderId) {
        Order order = orderService.queryOrderById(orderId);
        return R.ok(order);
    }


    @PostMapping("/queryOrders")
    public R<Page<QueryOrderDto>> queryOrders(@RequestBody @Validated QueryHisRequest request) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Page<QueryOrderDto> orderPage = orderService.queryOrders(userId.longValue(), request);
        return R.ok(orderPage);
    }

    @PostMapping("/queryOrdersByCondition")
    public R<Page<QueryOrderDto>> queryOrdersByCondition(@RequestBody @Validated QueryHisRequest request) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Page<QueryOrderDto> orderPage = orderService.queryOrdersByCondition(userId.longValue(), request);
        return R.ok(orderPage);
    }

    @PostMapping("/getAllOrderStatus")
    public R<OrderStatusCountDto> getAllOrderStatus() {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
        OrderStatusCountDto orderStatusCount = orderService.getAllOrderStatus(userId.longValue(), role);
        return R.ok(orderStatusCount);
    }

//    @PostMapping("/storeReceiveOrder")
//    @SaCheckRole("store")
//    public R<Void> storeReceiveOrder(@RequestBody StoreAcpOrderDto request){
//        Long orderId = request.getOrderId();
//        Long ownerId = request.getOwnerId();
//        // 鉴权
//        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
//        Order order = orderService.queryOrderById(orderId);
//        if (userIdFormat.longValue() != order.getStoreId()) {
//            throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能本人确认收货！");
//        }
//        orderService.updateDownTime(orderId);
//        return R.ok();
//    }
}
