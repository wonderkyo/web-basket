package cn.dhbin.isme.pms.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.entity.BasketReturn;
import cn.dhbin.isme.pms.service.BasketReturnService;
import cn.dhbin.isme.pms.service.BasketService;
import cn.dhbin.isme.pms.service.OrderService;
import cn.hutool.core.convert.NumberWithFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/basketOrder")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订单-周转筐")
public class BasketOrderController {
    private final BasketReturnService basketReturnService;
    @GetMapping("/acceptReturnBakset")
    public R<Void> acceptReturnBakset(@RequestParam Long returnId) {
        NumberWithFormat userId =
                (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        basketReturnService.acceptReturnBakset(returnId, userId.longValue());
        return R.ok();
    }
}
