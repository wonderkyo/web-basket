package cn.dhbin.isme.pms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.request.orderRequest;
import cn.dhbin.isme.pms.service.OrderDepotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orderDepot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "周转筐")
public class OrderDepotController {
    private final OrderDepotService orderDepotService;

    // 仓库接单，注解校验权限，如果不是ware身份就不能调用这个接口, @SaCheckRole({"ware", "admin"})
    @PostMapping("/wareReceiveOrder")
    @SaCheckRole("depot")
    public R<Void> wareReceiveOrder(@RequestBody @Validated orderRequest request) {
//        Object ss = StpUtil.getRoleList();
//        log.info("拿到的role是{}", ss);
        Long orderId = request.getOrderId();
        log.info("拿到的orderId是什么？？", orderId);
        orderDepotService.wareReceiveOrder(orderId);
        return R.ok();
    }

}
