package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.pms.domain.entity.BasketOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BasketOrderService extends IService<BasketOrder> {

    // 查询basket_list
    public String getBasketList(Long order_id);


}
