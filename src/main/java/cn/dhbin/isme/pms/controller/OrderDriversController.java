package cn.dhbin.isme.pms.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.QueryOrderDto;
import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.isme.pms.domain.entity.OrderDrivers;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import cn.dhbin.isme.pms.service.OrderDriversService;
import cn.hutool.core.convert.NumberWithFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/orderDrivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订单-司机")
public class OrderDriversController {
    private final OrderDriversService orderDriversService;

    @GetMapping("/driverAccept")
    public R<Void> driverAccept(@RequestParam Long orderId) {
        orderDriversService.driverAccept(orderId);
        return R.ok();
    }

    @PostMapping("/queryDriversOrder")
    public R<OrderDetailDto> queryDriversOrder(){
        OrderDetailDto orderDetail = orderDriversService.queryDriversOrder();
        return R.ok(orderDetail);
    }

    @PostMapping("/updateOrderDriverTime")
    public R<Void> updateOrderDriverTime(@RequestBody Map<String, String> request){
        String updateType = request.get("updateType");
        Long orderId = Long.parseLong(request.get("orderId"));
        String imgList = request.get("imgList");
        orderDriversService.updateOrderDriverTime(updateType, orderId, imgList);
        return R.ok();
    }

    @PostMapping("/queryDriverHisOrders")
    public R<Page<QueryOrderDto>> queryDriverHisOrders(@RequestBody @Validated QueryHisRequest request) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Page<QueryOrderDto> orderPage = orderDriversService.queryDriverHisOrders(userId.longValue(), request);
        return R.ok(orderPage);
    }

    @PostMapping("/uploadImg")
    public R<String> uploadImg(@RequestParam("file") MultipartFile file, @RequestParam("orderId") Long orderId) {
        log.info("文件是：{}", file);
        String imgUrl = orderDriversService.uploadImg(file, orderId);
        return R.ok(imgUrl);
    }

}
