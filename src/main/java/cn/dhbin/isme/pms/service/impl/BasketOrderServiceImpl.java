package cn.dhbin.isme.pms.service.impl;

import cn.dhbin.isme.pms.domain.entity.BasketOrder;
import cn.dhbin.isme.pms.mapper.BasketOrderMapper;
import cn.dhbin.isme.pms.service.BasketOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketOrderServiceImpl extends ServiceImpl<BasketOrderMapper, BasketOrder> implements BasketOrderService {

    @Override
    public String getBasketList(Long orderId) {
        BasketOrder basketOrder = getBaseMapper().selectOne(new LambdaQueryWrapper<BasketOrder>().eq(BasketOrder::getOrderId, orderId));
        String basketList = basketOrder.getBasketList();
        return basketList;
    }

}
