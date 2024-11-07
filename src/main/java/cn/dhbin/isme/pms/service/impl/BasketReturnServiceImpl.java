package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.entity.BasketReturn;
import cn.dhbin.isme.pms.domain.request.ReturnBasketListRequest;
import cn.dhbin.isme.pms.mapper.BasketReturnMapper;
import cn.dhbin.isme.pms.service.BasketReturnService;
import cn.dhbin.isme.pms.util.Constants;
import cn.hutool.core.convert.NumberWithFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class BasketReturnServiceImpl extends ServiceImpl<BasketReturnMapper, BasketReturn> implements BasketReturnService {

   @Override
   public void acceptReturnBakset(Long returnId, Long userId){
       BasketReturn basketReturn = getBaseMapper().selectById(returnId);
       if(basketReturn.getDriverId() != userId){
           throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能本人接收！");
       } else {
           this.update(null, new UpdateWrapper<BasketReturn>().set("status", "2").eq("id", returnId));
       }
   }

   @Override
    public Page<BasketReturn> queryReturnBasket(ReturnBasketListRequest request){
       NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
       String role = (String)StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
       IPage<BasketReturn> brIPage = request.toPage();
       LambdaQueryWrapper<BasketReturn> basketReturnLambdaQueryWrapper = new LambdaQueryWrapper<>();
       if(role.equals(Constants.ROLE_STORE)){
           // 不限制status，接单和不接单的都要
           basketReturnLambdaQueryWrapper.eq(BasketReturn::getStoreId, userIdFormat.longValue()).orderByDesc(BasketReturn::getId);
       }else if(role.equals(Constants.ROLE_DRIVER)){
           // 只要待接单的
           basketReturnLambdaQueryWrapper.eq(BasketReturn::getDriverId, userIdFormat.longValue()).eq(BasketReturn::getStatus, "1").orderByDesc(BasketReturn::getId);
       }
       getBaseMapper().selectPage(brIPage, basketReturnLambdaQueryWrapper);
       Page<BasketReturn> basketReturnPage = new Page<>();
       basketReturnPage.setPageData(brIPage.getRecords());
       basketReturnPage.setTotal(brIPage.getTotal());
       return basketReturnPage;
   }

   @Override
   public void acceptReturnReq(Long returnId){
       NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
       BasketReturn basketReturn = new BasketReturn();
       basketReturn.setId(returnId);
       basketReturn.setStatus("2");
       getBaseMapper().updateById(basketReturn);
   }
}
