package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.pms.domain.dto.BasketTotalDto;
import cn.dhbin.isme.pms.domain.dto.MyBasketDto;
import cn.dhbin.isme.pms.domain.entity.Basket;
import cn.dhbin.isme.pms.domain.request.QueryMyBasketRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BasketService extends IService<Basket> {
    Map uploadFile(MultipartFile file);
    BasketTotalDto queryBasket(String queryType, Long pageNo, Long pageSize, String queryColumn, String queryCondition);
    Map outBasket(Long orderId);
    void modifyBasketStatus(String basketRfid, String status);
    void modifyBasketStatusBatch(List<String> basketList, String status, Long userId);
    Map pdaBatchBasket();

    void receiveOrder(Long orderId, Long ownerId);

    // 查询用户名下的basket
    MyBasketDto queryMyBasket(Long userId, QueryMyBasketRequest request);

    void modifyBatchBasket(Long orderId, String status, Long userId);

    // 归还basket的请求
    String returnBasket(Long driverId, Long storeId);

    void acceptReturnBasket(Long returnId);
}
