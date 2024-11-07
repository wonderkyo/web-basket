package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BadRequestException;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.OrderStatusCountDto;
import cn.dhbin.isme.pms.domain.dto.QueryOrderDto;
import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.isme.pms.domain.entity.OrderDepot;
import cn.dhbin.isme.pms.domain.entity.OrderDrivers;
import cn.dhbin.isme.pms.domain.request.CreateOrderRequest;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import cn.dhbin.isme.pms.mapper.OrderMapper;
import cn.dhbin.isme.pms.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.dhbin.isme.pms.util.Constants;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setProductName(request.getProductName());
        order.setProductQuant(request.getProductQuant());
        order.setStoreId(request.getStoreId());
        order.setStatus(Constants.ORDER_CREATE);
        order.setAddress(request.getAddress());
        order.setRecName(request.getRecName());
        order.setRecPhone(request.getRecPhone());
        save(order);
    }

//    @Override
//    public Page<HisOrdersDto> queryHisOrders(Long userId, QueryHisRequest request){
//        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
//        String queryType = request.getQueryType();
//        IPage<HisOrdersDto> orderParam = request.toPage();
//        IPage<HisOrdersDto>  orderPage = getBaseMapper().queryHisOrders(orderParam, userId, role, queryType);
//        return Page.convert(orderPage);
//    }

    @Override
    public OrderStatusCountDto getAllOrderStatus(Long userId, String role){
        Long createCount = getCountFunc(Constants.ORDER_CREATE, role, userId);
        Long wareAcpCount = getCountFunc(Constants.ORDER_WARE_ACP, role, userId);
        Long driverAcpCount = getCountFunc(Constants.ORDER_DRIVER_ACP, role, userId);
        Long outWareCount = getCountFunc(Constants.ORDER_OUT_WARE, role, userId);
        Long driverBeginCount = getCountFunc(Constants.ORDER_DRIVER_BEGIN, role, userId);
        Long driveArriveCount = getCountFunc(Constants.ORDER_DRIVER_ARRIVE, role, userId);
        Long storeAcpCount = getCountFunc(Constants.ORDER_STORE_ACP, role, userId);
        OrderStatusCountDto orderStatusCountDto = new OrderStatusCountDto();
        orderStatusCountDto.setCreateCount(createCount);
        orderStatusCountDto.setWareAcpCount(wareAcpCount);
        orderStatusCountDto.setDriverAcpCount(driverAcpCount);
        orderStatusCountDto.setOutWareCount(outWareCount);
        orderStatusCountDto.setDriverBeginCount(driverBeginCount);
        orderStatusCountDto.setDriveArriveCount(driveArriveCount);
        orderStatusCountDto.setStoreAcpCount(storeAcpCount);
        return orderStatusCountDto;
    }
    private Long getCountFunc(String status, String role, Long userId){
        if (role.equals(Constants.ROLE_DEPOT)) {
            // 仓库
            if(role.equals(Constants.ROLE_DEPOT) && status.equals(Constants.ORDER_CREATE)){
                return getBaseMapper().selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, Constants.ORDER_CREATE));
            } else {
                return getBaseMapper().selectJoinCount(new MPJLambdaWrapper<Order>()
                        .leftJoin(OrderDepot.class, OrderDepot::getOrderId, Order::getOrderId)
                        .eq(OrderDepot::getWareId, userId)
                        .orderByDesc(Order::getCreateTime)
                        .eq(Order::getStatus, status));
            }
        } else if(role.equals(Constants.ROLE_STORE)){
            // 门店
            return getBaseMapper().selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStoreId, userId).eq(Order::getStatus, status));
        } else if(role.equals(Constants.ROLE_ADMIN)){
            // 管理员
            return getBaseMapper().selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, status));
        } else{
            // 司机
            return getBaseMapper().selectJoinCount(new MPJLambdaWrapper<Order>()
                    .leftJoin(OrderDrivers.class, OrderDrivers::getOrderId, Order::getOrderId)
                    .eq(OrderDrivers::getDriverId, userId)
                    .orderByDesc(Order::getCreateTime)
                    .eq(Order::getStatus, status));
        }
    }

    // 用来分页查询订单，可以选择状态queryStatus, 查询自己的，没接单的不算
    @Override
    public Page<QueryOrderDto> queryOrders(Long userId, QueryHisRequest request) {
        // 查询的状态
        String queryStatus = request.getQueryStatus();
        // request
        IPage<QueryOrderDto> orderParam = request.toPage();
        IPage<Order> orderIPage = request.toPage();
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        MPJLambdaWrapper<Order> objectMPJLambdaWrapper = new MPJLambdaWrapper<>();
        // role角色
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
        if (role.equals(Constants.ROLE_STORE)) {
            if (!(queryStatus == null || queryStatus.isEmpty())) {
                if (queryStatus.equals("unDown")) {
                    queryWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                } else {
                    queryWrapper.eq(Order::getStatus, queryStatus);
                }
            }
            // 商店就是用户名和门店号相等
            queryWrapper.eq(Order::getStoreId, userId).orderByDesc(Order::getCreateTime);
            orderParam = getBaseMapper().selectPage(orderIPage, queryWrapper).convert(order -> {
                QueryOrderDto queryOrderDto = new QueryOrderDto();
                BeanUtils.copyProperties(order, queryOrderDto);
                return queryOrderDto;
            });
        } else if (role.equals(Constants.ROLE_DEPOT)) {
            // 仓库,和order_depot联表查
            objectMPJLambdaWrapper.selectAll(Order.class)
                    .leftJoin(OrderDepot.class, OrderDepot::getOrderId, Order::getOrderId)
                    .eq(OrderDepot::getWareId, userId)
                    .orderByDesc(Order::getCreateTime);
            if (!(queryStatus == null || queryStatus.isEmpty())) {
                if (queryStatus.equals("unDown")) {
                    objectMPJLambdaWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                } else {
                    objectMPJLambdaWrapper.eq(Order::getStatus, queryStatus);
                }
            }
            orderParam = getBaseMapper().selectJoinPage(
                    orderParam,
                    QueryOrderDto.class,
                    objectMPJLambdaWrapper);

        } else if (role.equals(Constants.ROLE_DRIVER)) {
            if (queryStatus.equals(Constants.ORDER_WARE_ACP)) {
                objectMPJLambdaWrapper.selectAll(Order.class)
                        .selectAs(OrderDepot::getWareAddress, "wareAddress")
                        .leftJoin(OrderDepot.class, OrderDepot::getOrderId, Order::getOrderId)
                        .eq(Order::getStatus, queryStatus)
                        .orderByDesc(Order::getCreateTime);
            } else {
                // 司机，和order_drivers联表查询
                objectMPJLambdaWrapper.selectAll(Order.class)
                        .leftJoin(OrderDrivers.class, OrderDrivers::getOrderId, Order::getOrderId)
                        .orderByDesc(Order::getCreateTime);
                // 没完结的
                if (queryStatus.equals("unDown")) {
                    objectMPJLambdaWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                } else {
                    objectMPJLambdaWrapper.eq(OrderDrivers::getDriverId, userId).eq(Order::getStatus, queryStatus);
                }
            }

            orderParam = getBaseMapper().selectJoinPage(
                    orderParam,
                    QueryOrderDto.class,
                    objectMPJLambdaWrapper);

        } else if (role.equals(Constants.ROLE_ADMIN)) {
            // 查全部
            if (!(queryStatus == null || queryStatus.isEmpty())) {
                if (queryStatus.equals("unDown")) {
                    queryWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                } else {
                    queryWrapper.eq(Order::getStatus, queryStatus);
                }
            }

            queryWrapper.orderByDesc(Order::getCreateTime);
            orderParam = getBaseMapper().selectPage(orderIPage, queryWrapper).convert(order -> {
                QueryOrderDto queryOrderDto = new QueryOrderDto();
                BeanUtils.copyProperties(order, queryOrderDto);
                return queryOrderDto;
            });
        } else {
            throw new BadRequestException("不要越权哦！！");
        }

        // new一个返回体
        Page<QueryOrderDto> orderPage = new Page<>();
        orderPage.setPageData(orderParam.getRecords());
        orderPage.setTotal(orderParam.getTotal());
        return orderPage;
    }

    // 可以查不同的查询条件的
    @Override
    public Page<QueryOrderDto> queryOrdersByCondition(Long userId, QueryHisRequest request) {
        // 查询的状态
        String queryStatus = request.getQueryStatus();
        // request
        IPage<QueryOrderDto> orderParam = request.toPage();
        IPage<Order> orderIPage = request.toPage();
        String queryColumn = request.getQueryColumn();

        String queryValue = request.getQueryValue(); // 这个是查询的数值
        //

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        MPJLambdaWrapper<Order> objectMPJLambdaWrapper = new MPJLambdaWrapper<>();
        // role角色
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
        if (role.equals(Constants.ROLE_STORE)) {
            if (!(queryColumn == null || queryColumn.isEmpty()) && !(queryValue == null || queryValue.isEmpty())) {
                if("orderId".equals(queryColumn)){
                    queryWrapper.eq(Order::getOrderId, Long.parseLong(queryValue));
                }else if("productName".equals(queryColumn)){
                    queryWrapper.eq(Order::getProductName, queryValue);
                }else if("status".equals(queryColumn)){
                    if (request.getQueryValue().equals("unDown")) {
                        queryWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                    } else {
                        queryWrapper.eq(Order::getStatus, queryValue);
                    }
                } else {
                    // 无查询条件
                }
            }
            // 商店就是用户名和门店号相等
            queryWrapper.eq(Order::getStoreId, userId).orderByDesc(Order::getCreateTime);
            orderParam = getBaseMapper().selectPage(orderIPage, queryWrapper).convert(order -> {
                QueryOrderDto queryOrderDto = new QueryOrderDto();
                BeanUtils.copyProperties(order, queryOrderDto);
                return queryOrderDto;
            });
        } else if (role.equals(Constants.ROLE_DEPOT)) {
            // 仓库,和order_depot联表查
            objectMPJLambdaWrapper.selectAll(Order.class)
                    .leftJoin(OrderDepot.class, OrderDepot::getOrderId, Order::getOrderId)
                    .eq(OrderDepot::getWareId, userId)
                    .orderByDesc(Order::getCreateTime);
            if (!(queryColumn == null || queryColumn.isEmpty()) && !(queryValue == null || queryValue.isEmpty())) {
                if("orderId".equals(queryColumn)){
                    objectMPJLambdaWrapper.eq(Order::getOrderId, Long.parseLong(queryValue));
                }else if("productName".equals(queryColumn)){
                    objectMPJLambdaWrapper.eq(Order::getProductName, queryValue);
                }else if("status".equals(queryColumn)){
                    if (request.getQueryValue().equals("unDown")) {
                        objectMPJLambdaWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                    } else {
                        objectMPJLambdaWrapper.eq(Order::getStatus, queryValue);
                    }
                } else {
                    // 无查询条件
                }
            }
            orderParam = getBaseMapper().selectJoinPage(
                    orderParam,
                    QueryOrderDto.class,
                    objectMPJLambdaWrapper);

        } else if (role.equals(Constants.ROLE_ADMIN)) {
            // 查全部
            if (!(queryColumn == null || queryColumn.isEmpty()) && !(queryValue == null || queryValue.isEmpty())) {
                if("orderId".equals(queryColumn)){
                    queryWrapper.eq(Order::getOrderId, Long.parseLong(queryValue));
                }else if("productName".equals(queryColumn)){
                    queryWrapper.eq(Order::getProductName, queryValue);
                }else if("status".equals(queryColumn)){
                    if (request.getQueryValue().equals("unDown")) {
                        queryWrapper.ne(Order::getStatus, Constants.ORDER_STORE_ACP);
                    } else {
                        queryWrapper.eq(Order::getStatus, queryValue);
                    }
                } else {
                    // 无查询条件
                }
            }
            queryWrapper.orderByDesc(Order::getCreateTime);
            orderParam = getBaseMapper().selectPage(orderIPage, queryWrapper).convert(order -> {
                QueryOrderDto queryOrderDto = new QueryOrderDto();
                BeanUtils.copyProperties(order, queryOrderDto);
                return queryOrderDto;
            });
        } else {
            throw new BadRequestException("不要越权哦！！");
        }

        // new一个返回体
        Page<QueryOrderDto> orderPage = new Page<>();
        orderPage.setPageData(orderParam.getRecords());
        orderPage.setTotal(orderParam.getTotal());
        return orderPage;
    }
    @Override
    public OrderDetailDto getOrderDetails(Long orderId) {
        OrderDetailDto orderDetail = getBaseMapper().orderDetail(orderId);
        return orderDetail;
    }

    // 查询没有被接单的订单，用的status==create判断
    @Override
    public Page<Order> queryOnlyOrdersPage(QueryHisRequest request) {
        IPage<Order> pageParam = request.toPage();
        String status = request.getQueryStatus();
        // 查询
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, status).orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = getBaseMapper().selectPage(pageParam, queryWrapper);
        return Page.convert(orderPage);
    }

    @Override
    // 当一个方法用 @Transactional 注解标记时，Spring 将在这个方法的执行期间创建一个事务。这意味着方法开始时事务开始，正常结束时事务提交，如果在方法执行过程中抛出异常则事务回滚。
    public void modifyOrderStatus(Long orderId, String status) {
//        Order order = new Order();
//        order.setStatus(status);
//        this.updateById(order);
        this.update(null, new UpdateWrapper<Order>().set("status", status).eq("order_id", orderId));
        // 惊天大bug，为什么每次调用更新status的方法之后，order表里面的product_quant都被重置成0？
        // updateById以及所有的update方法，只要你传了实体，都会更新所有不为null的字段，int的默认值是0，要用Integer，它默认值是null
    }

    @Override
    public Order queryOrderById(Long orderId){
        Order order = getBaseMapper().selectById(orderId);
        return order;
    }

    public void updateDownTime(Long orderId){
        // 更新收货时间
        this.update(null, new UpdateWrapper<Order>().set("down_time", LocalDateTime.now()).set("status", Constants.ORDER_STORE_ACP ).eq("order_id", orderId));
    }

}
