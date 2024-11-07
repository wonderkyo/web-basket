package cn.dhbin.isme.pms.mapper;

import cn.dhbin.isme.pms.domain.dto.BasketInfoDto;
import cn.dhbin.isme.pms.domain.entity.Basket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BasketMapper extends BaseMapper<Basket> {
    void insertBatchBasket(List<String> basketList, Long userId);
    void rukuBatchBasket(String baskRfid, Long userId, String status);
    IPage<BasketInfoDto> queryBasketSingle(Page<BasketInfoDto> page, @Param("queryColumn") String queryColumn, @Param("queryCondition") String queryCondition);

    void modifyBatchBasket(List<String> basketList, String status, Long userId, Long orderId);

}
