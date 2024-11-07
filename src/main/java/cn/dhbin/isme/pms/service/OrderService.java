package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.OrderStatusCountDto;
import cn.dhbin.isme.pms.domain.dto.QueryOrderDto;
import cn.dhbin.isme.pms.domain.dto.UnWareDto;
import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.isme.pms.domain.request.CreateOrderRequest;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<Order> {

    /**
     * 门店发起订单
     *
     * @param request 请求
     * @return 空
     */
    void createOrder(CreateOrderRequest request);

//    Page<HisOrdersDto> queryHisOrders(Long userId, QueryHisRequest request);

    OrderDetailDto getOrderDetails(Long orderId);

    // 可接单的，除了order的信息之外，还要加下单人的profile
    Page<Order> queryOnlyOrdersPage(QueryHisRequest request);

    // 只查单个的order表的信息
    Order queryOrderById(Long orderId);

    void modifyOrderStatus(Long orderId, String status);

    // 别的状态的，只需要分页查询order就行，其余的放在具体信息单个查
    // 拟写一个queryOrders的接口
    Page<QueryOrderDto> queryOrders(Long userId, QueryHisRequest request);

    Page<QueryOrderDto> queryOrdersByCondition(Long userId, QueryHisRequest request);
    void updateDownTime(Long orderId);

    OrderStatusCountDto getAllOrderStatus(Long userId, String role);

}
