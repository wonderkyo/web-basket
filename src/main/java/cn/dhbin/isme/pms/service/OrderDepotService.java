package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.pms.domain.entity.OrderDepot;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderDepotService extends IService<OrderDepot> {
    // 仓库接单
    void wareReceiveOrder(Long orderId);

    // 仓库出库
    void wareOutOrder(Long orderId);
}
