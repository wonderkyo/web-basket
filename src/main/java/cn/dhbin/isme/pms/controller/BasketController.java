package cn.dhbin.isme.pms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.dto.BasketTotalDto;
import cn.dhbin.isme.pms.domain.dto.MyBasketDto;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.StoreAcpOrderDto;
import cn.dhbin.isme.pms.domain.request.ModifyBastketRequest;
import cn.dhbin.isme.pms.domain.request.QueryMyBasketRequest;
import cn.dhbin.isme.pms.domain.request.queryBasketRequest;
import cn.hutool.core.convert.NumberWithFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.dhbin.isme.pms.service.BasketService;

import java.util.Map;

@RestController
@RequestMapping("/basket")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "周转筐")
public class BasketController {

    private final BasketService basketService;

    @PostMapping("/uploadFile")
    public R<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("文件是：{}", file);
        Map<String, Object> basketCount = basketService.uploadFile(file);
        return R.ok(basketCount);
    }

    @PostMapping("/pdaBatchBasket")
    public R<Map<String, Object>> pdaBatchBasket() {
        Map<String, Object> basketCount = basketService.pdaBatchBasket();
        return R.ok(basketCount);
    }

    // 查询周转筐的总量和分页查询
    @PostMapping("/queryBasket")
    public R<BasketTotalDto> queryBasket(@RequestBody @Validated queryBasketRequest request) {
        String queryType = request.getQueryType();
        Long pageNo = request.getPageNo();
        Long pageSize = request.getPageSize();

        String queryColumn = request.getQueryColumn();
        String queryCondition = request.getQueryCodition();

        BasketTotalDto basketTotalInfo = basketService.queryBasket(queryType, pageNo, pageSize, queryColumn, queryCondition);
        return R.ok(basketTotalInfo);
    }

    // 修改周转筐的状态
    @PostMapping("/modifyBasketStatus")
    public R<Void> modifyBasketStatus(@RequestBody Map<String, String> request) {
        String basketRfid = request.get("basketRfid");
        String status = request.get("status");
        basketService.modifyBasketStatus(basketRfid, status);
        return R.ok();
    }

    // 出库,只能是仓库或者管理员有权限
    @SaCheckRole(value = {"depot", "admin"}, mode = SaMode.OR)
    @PostMapping("/outBasket")
    public R<Map<String, Object>> outBasket(@RequestBody Map<String, ?> request) {
        // 因为Integer和long不兼容
        Number orderIdNum = (Number) request.get("orderId");
        Long orderId = (orderIdNum != null) ? orderIdNum.longValue() : null;
        Map<String, Object> res = basketService.outBasket(orderId);
        return R.ok(res);
    }

    // 门店接收订单
    @PostMapping("/receiveOrder")
    public R<Void> receiveOrder(@RequestBody StoreAcpOrderDto request) {
        Long orderId = request.getOrderId();
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        // 鉴权
        basketService.receiveOrder(orderId, userId.longValue());
        return R.ok();
    }

    @PostMapping("/queryMyBasket")
    public R<MyBasketDto> queryMyBasket(@RequestBody @Validated QueryMyBasketRequest request) {
        // 用户名下的筐子
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        MyBasketDto myBasketDto = basketService.queryMyBasket(userId.longValue(), request);
        return R.ok(myBasketDto);
    }

    @PostMapping("/modifyBatchBasket")
    public R<Void> modifyBatchBasket(@RequestBody @Validated ModifyBastketRequest request) {
        String status = request.getStatus();
        Long orderId = request.getOrderId();
        NumberWithFormat userId = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        basketService.modifyBatchBasket(orderId, status, userId.longValue());
        return R.ok();
    }

    @GetMapping("/returnBasket")
    public R<String> returnBasket(@RequestParam Long driverId) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        String basketListStr = basketService.returnBasket(driverId, userId.longValue());
        return R.ok(basketListStr);
    }

    @GetMapping("/acceptReturnBasket")
    public R<Void> acceptReturnBasket(@RequestParam Long returnId){
        basketService.acceptReturnBasket(returnId);
        return R.ok();
    }
}
