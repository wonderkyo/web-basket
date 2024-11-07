package cn.dhbin.isme.pms.mapper;

import cn.dhbin.isme.pms.domain.dto.HisOrdersDto;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.UnWareDto;
import cn.dhbin.isme.pms                                                                                                                                                .domain.entity.Order;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends MPJBaseMapper<Order> {
//    查询订单详细信息
    OrderDetailDto orderDetail(@Param("orderId") Long orderId);
    IPage<UnWareDto> unWareOrders(IPage<UnWareDto> page, String statusParam);

    IPage<HisOrdersDto> queryHisOrders(IPage<HisOrdersDto> page, @Param("userId") Long userId, @Param("role") String role, @Param("queryType") String queryType);
}

