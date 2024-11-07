package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.dto.OrderDetailDto;
import cn.dhbin.isme.pms.domain.dto.QueryOrderDto;
import cn.dhbin.isme.pms.domain.entity.OrderDrivers;
import cn.dhbin.isme.pms.domain.request.QueryHisRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface OrderDriversService extends IService<OrderDrivers> {
    void driverAccept(Long orderId);

    OrderDetailDto queryDriversOrder();

    void updateOrderDriverTime(String updateType, Long orderId, String imgList);

    Page<QueryOrderDto> queryDriverHisOrders(Long userId, QueryHisRequest request);

    String uploadImg(MultipartFile file, Long orderId);

}
