package cn.dhbin.isme.pms.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.entity.BasketReturn;
import cn.dhbin.isme.pms.domain.request.ReturnBasketListRequest;
import cn.dhbin.isme.pms.service.BasketReturnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basketReturn")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "返回订单-周转筐")
public class BasketReturnController {
    private final BasketReturnService basketReturnService;

    @PostMapping("/queryReturnBasket")
    public R<Page<BasketReturn>> queryReturnBasket(@RequestBody @Validated ReturnBasketListRequest request) {
        Page<BasketReturn> returnBasket = basketReturnService.queryReturnBasket(request);
        return R.ok(returnBasket);
    }

    @GetMapping("/acceptReturnReq")
    public R<Void> acceptReturnReq(@RequestParam Long returnId){
        basketReturnService.acceptReturnReq(returnId);
        return R.ok();
    }

}
