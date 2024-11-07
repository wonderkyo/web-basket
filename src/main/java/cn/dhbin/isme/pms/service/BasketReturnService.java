package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.entity.BasketReturn;
import cn.dhbin.isme.pms.domain.request.ReturnBasketListRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BasketReturnService extends IService<BasketReturn> {

    void acceptReturnBakset(Long returnId, Long userId);

    Page<BasketReturn> queryReturnBasket(ReturnBasketListRequest request);

    void acceptReturnReq(Long returnId);
}
