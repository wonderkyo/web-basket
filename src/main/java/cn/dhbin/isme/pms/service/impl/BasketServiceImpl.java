package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BadRequestException;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.pms.domain.dto.BasketInfoDto;
import cn.dhbin.isme.pms.domain.dto.BasketTotalDto;
import cn.dhbin.isme.pms.domain.dto.MyBasketDto;
import cn.dhbin.isme.pms.domain.entity.Basket;
import cn.dhbin.isme.pms.domain.entity.BasketOrder;
import cn.dhbin.isme.pms.domain.entity.BasketReturn;
import cn.dhbin.isme.pms.domain.entity.OrderDepot;
import cn.dhbin.isme.pms.domain.request.QueryMyBasketRequest;
import cn.dhbin.isme.pms.mapper.BasketMapper;
import cn.dhbin.isme.pms.service.*;
import cn.dhbin.isme.pms.util.Constants;
import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketServiceImpl extends ServiceImpl<BasketMapper, Basket> implements BasketService {

    private final OrderDepotService orderDepotService;

    private final BasketOrderService basketOrderService;
    private final BasketReturnService basketReturnService;
    private final OrderService orderService;

    private static final String FTP_URL = "192.168.1.10"; // FTP服务器地址
    private static final int FTP_PORT = 6666; // FTP服务器端口
    private static final String OUT_BASKET_REMOTE_PATH = "/ck_仓库出库.xls";
    private static final String INSERT_BASKET_REMOTE_PATH = "/ck_管理员录入.xls";
    private static final String DEPOT_BASKET_REMOTE_PATH = "/ck_仓库入库.xls";
    private static final String RETURN_BASKET_REMOTE_PATH = "/ck_门店归还.xls";

    // 查询有周转筐的情况，包含总数，占用，空闲，损坏，以及具体的信息
    @Override
    public BasketTotalDto queryBasket(String queryType, Long pageNo, Long pageSize, String queryColumn, String queryCondition) {
        BasketTotalDto basketTotalInfo = new BasketTotalDto();
        if ("all".equals(queryType)) {
            // 只查询总体情况的
            Long occupiedAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getStatus, Constants.Basket_Occupied_KEY));
            Long freeAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getStatus, Constants.Basket_Free_KEY));
            Long brokenAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getStatus, Constants.Basket_Broken));
            Long lostAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getStatus, Constants.Basket_Lost)); // 遗失
            Long returnAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getStatus, Constants.Basket_RETURN)); // 遗失


            basketTotalInfo.setOccupiedAmount(occupiedAmount);
            basketTotalInfo.setFreeAmount(freeAmount);
            basketTotalInfo.setBrokenAmount(brokenAmount);
            basketTotalInfo.setLostAmount(lostAmount);
            basketTotalInfo.setReturnAmount(returnAmount);
        }
        // 分页查询的，其实最好分两个接口写，但是懒得分了
        Page<BasketInfoDto> page = new Page<>(pageNo, pageSize);
        // 分页查询，按照条件
        if (queryColumn != null && queryCondition != null) {
            // 1.转换列名
            Map<String, String> queryColumnRevert = new HashMap<>();
            queryColumnRevert.put("basketRfid", "b.basket_rfid");
            queryColumnRevert.put("basketStatus", "b.status");
            queryColumnRevert.put("createTime", "b.create_time");
            queryColumnRevert.put("basketLoc", "pr.address");
            queryColumnRevert.put("userId", "u.id");
            queryColumnRevert.put("role", "u.role");
            queryColumnRevert.put("updateTime", "b.update_time");
            String queryColumnReverted = queryColumnRevert.get(queryColumn);
            // 2.模糊查询
            getBaseMapper().queryBasketSingle(page, queryColumnReverted, queryCondition);
        } else {
            getBaseMapper().queryBasketSingle(page, "", "");
        }

        // 查询到的列表
        List<BasketInfoDto> records = page.getRecords();
        basketTotalInfo.setBasketList(records);
        basketTotalInfo.setTotalAmount(page.getTotal());
        return basketTotalInfo;

    }

    @Override
    public MyBasketDto queryMyBasket(Long userId, QueryMyBasketRequest request) {
        Long searchId = null;
        if(request.getQueryId() == null){
            searchId = userId;
        }else{
            searchId = request.getQueryId();
        }
        MyBasketDto myBasketDto = new MyBasketDto();
        Long totalAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId)); // 总数
        Long occupiedAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId).eq(Basket::getStatus, Constants.Basket_Occupied_KEY)); // 使用中
        Long freeAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId).eq(Basket::getStatus, Constants.Basket_Free_KEY)); // 空闲
        Long brokenAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId).eq(Basket::getStatus, Constants.Basket_Broken)); // 破损
        Long lostAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId).eq(Basket::getStatus, Constants.Basket_Lost)); // 遗失
        Long returnAmount = this.count(new LambdaQueryWrapper<Basket>().eq(Basket::getUserId, searchId).eq(Basket::getStatus, Constants.Basket_RETURN)); // 遗失

        LambdaQueryWrapper<Basket> queryWrapper = new LambdaQueryWrapper<>();
        IPage<Basket> pageParam = request.toPage();
        queryWrapper.eq(Basket::getUserId, searchId).orderByDesc(Basket::getUpdateTime);

        if (request.getQueryStatus() != null && !request.getQueryStatus().equals("")) {
            queryWrapper.eq(Basket::getStatus, request.getQueryStatus());
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            LocalDateTime startTime = Instant.ofEpochMilli(request.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime endTime = Instant.ofEpochMilli(request.getEndTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            queryWrapper.between(Basket::getUpdateTime, startTime, endTime);
        }

        IPage<Basket> basketPage = getBaseMapper().selectPage(pageParam, queryWrapper);
        myBasketDto.setBasketList(basketPage.getRecords());
        myBasketDto.setTotal(basketPage.getTotal());

        myBasketDto.setTotalAmount(totalAmount);
        myBasketDto.setOccupiedAmount(occupiedAmount);
        myBasketDto.setFreeAmount(freeAmount);
        myBasketDto.setBrokenAmount(brokenAmount);
        myBasketDto.setLostAmount(lostAmount);
        myBasketDto.setReturnAmount(returnAmount);

        return myBasketDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadFile(MultipartFile file) {
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Long userId = userIdFormat.longValue();
        // 1. 判断文件格式
        String fileName = file.getOriginalFilename();
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = fileName.endsWith(".xls") ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)
            ) {
                Sheet sheet = workbook.getSheetAt(0);
                int secondColumnIndex = 1; // 假设第二列是索引为1的列
                List<String> basketList = new ArrayList<>(); // 拿到了rfid的list
                for (Row row : sheet) {
                    Cell cell = row.getCell(secondColumnIndex);
                    if (cell != null) {
                        String rfid = cell.getStringCellValue();
                        basketList.add(rfid);
                    }
                }
                System.out.println(basketList); // 处理读取的数据

                // 批量导入rfid的数据
                if(role.equals(Constants.ROLE_DEPOT)){
                    // 仓库-执行入库操作
                    // 仓库-执行入库操作
                    for(String baskRfid: basketList){
                        getBaseMapper().rukuBatchBasket(baskRfid, userId, Constants.Basket_Free_KEY);
                    }
                    Map<String, Object> res = new HashMap<>();
                    res.put("insertCount", basketList.size());
                    res.put("basketList", basketList);
                    return res;
                    // 管理员-执行录入操作
                } else if(role.equals(Constants.ROLE_ADMIN)){
                    Long countBefore = getBaseMapper().selectCount(null);
                    getBaseMapper().insertBatchBasket(basketList, userId);
                    Long countAfter = getBaseMapper().selectCount(null);
                    Map<String, Object> res = new HashMap<>();
                    res.put("insertCount", countAfter - countBefore);
                    res.put("allCount", (long) basketList.size());
                    res.put("basketList", basketList);
                    return res;
                } else {
                    throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能司机/管理员调用！");
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new BadRequestException("文件读取失败");
            }
        } else {
            throw new BadRequestException("文件格式有误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> outBasket(Long orderId) {
        // 防止越权，看ware_id和当前的userId是否一致
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Long userId = userIdFormat.longValue();
        Long wareId = orderDepotService.getBaseMapper().selectOne(new LambdaQueryWrapper<OrderDepot>().select(OrderDepot::getWareId).eq(OrderDepot::getOrderId, orderId)).getWareId();
        if (userId != wareId) {
            throw new BadRequestException("请勿越权操作！！");
        }
        // 开始读远程文件，并且修改basketList的状态和userId

        // 读取basketList
        List<String> basketList = GetBasketList(FTP_URL, FTP_PORT, OUT_BASKET_REMOTE_PATH);

        // 批量修改basket的状态
        getBaseMapper().modifyBatchBasket(basketList, Constants.Basket_Occupied_KEY, userId, orderId);
        BasketOrder basketOrder = new BasketOrder();
        String basketListStr = new JSONArray(basketList).toString();
        basketOrder.setBasketList(basketListStr);
        basketOrder.setOrderId(orderId);
        basketOrderService.save(basketOrder);

        // 更新order_depot表里面的出库时间update_time
        orderDepotService.wareOutOrder(orderId);

        // 返回order的信息，以及绑定的basketListStr是什么
        Map<String, Object> res = new HashMap<>();
        res.put("basketListStr", basketListStr);
        res.put("orderId", orderId);
        return res;
    }

    @Override
    public void modifyBatchBasket(Long orderId, String status, Long userId) {

        String basketListStr = basketOrderService.getBasketList(orderId);
        Gson gson = new Gson();
        // 定义 List<String> 的类型
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        // 使用 Gson 将 JSON 字符串转换为 List<String>
        List<String> basketList = gson.fromJson(basketListStr, listType);
        getBaseMapper().modifyBatchBasket(basketList, status, userId, orderId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> pdaBatchBasket() {
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);

        Long userId = userIdFormat.longValue();
        // 批量导入rfid的数据
        if(role.equals(Constants.ROLE_DEPOT)){
            List<String> basketList = GetBasketList(FTP_URL, FTP_PORT, DEPOT_BASKET_REMOTE_PATH);

            // 仓库-执行入库操作
            for(String baskRfid: basketList){
                getBaseMapper().rukuBatchBasket(baskRfid, userId, Constants.Basket_Free_KEY);
            }

            Map<String, Object> res = new HashMap<>();
            res.put("insertCount", basketList.size());
            res.put("basketList", basketList);
            return res;
            // 管理员-执行录入操作
        } else if(role.equals(Constants.ROLE_ADMIN)){
            List<String> basketList = GetBasketList(FTP_URL, FTP_PORT, INSERT_BASKET_REMOTE_PATH);
            Long countBefore = getBaseMapper().selectCount(null);
            getBaseMapper().insertBatchBasket(basketList, userId);
            Long countAfter = getBaseMapper().selectCount(null);
            Map<String, Object> res = new HashMap<>();
            res.put("insertCount", countAfter - countBefore);
            res.put("allCount", (long) basketList.size());
            res.put("basketList", basketList);
            return res;
        } else {
            throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能司机/管理员调用！");
        }
    }



    // 门店发起的归还basket请求
    @Override
    public String returnBasket(Long driverId, Long storeId) {
        List<String> basketList = GetBasketList(FTP_URL, FTP_PORT, RETURN_BASKET_REMOTE_PATH);
        String basketListStr = new JSONArray(basketList).toString();
        BasketReturn basketReturn = new BasketReturn();
        basketReturn.setBasketList(basketListStr);
        basketReturn.setDriverId(driverId);
        basketReturn.setStoreId(storeId);
        basketReturnService.save(basketReturn);
        return basketListStr;
    }

    @Override
    public void acceptReturnBasket(Long returnId){
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        // 拿到list并且开始修改状态
        basketReturnService.acceptReturnReq(returnId);
        BasketReturn basketReturn1 = basketReturnService.getBaseMapper().selectById(returnId);
        String basketListStr = basketReturn1.getBasketList();
        Gson gson = new Gson();
        // 定义 List<String> 的类型
        Type listType = new TypeToken<List<String>>() {}.getType();
        // 使用 Gson 将 JSON 字符串转换为 List<String>
        List<String> basketList = gson.fromJson(basketListStr, listType);
        modifyBasketStatusBatch(basketList, Constants.Basket_RETURN, userIdFormat.longValue());
    }

    private List<String> GetBasketList(String ftpUrl, int ftpPort, String remoteFilePath) {
        List<String> basketList = new ArrayList<>(); // 拿到了rfid的list
        InputStream inputStream = null;
        // 获取流
        try {
            inputStream = FTPConnector(ftpUrl, ftpPort, remoteFilePath);
        } catch (Exception e) {
            throw new BadRequestException("连接FTP服务器失败");
        }
        // 开始读
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int secondColumnIndex = 1; // 假设第二列是索引为1的列

            for (Row row : sheet) {
                Cell cell = row.getCell(secondColumnIndex);
                if (cell != null) {
                    String rfid = cell.getStringCellValue();
                    basketList.add(rfid);
                }
            }
            System.out.println(basketList); // 处理读取的数据
            return basketList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return basketList;
        }
    }

    // 写连接FTP服务器并且获取文件列表
    private InputStream FTPConnector(String ftpUrl, int ftpPort, String remoteFilePath) {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");

        try {
            // 连接
            ftpClient.connect(ftpUrl, ftpPort);
            // 匿名登录
            boolean loginResult = ftpClient.login("anonymous", "password");
            if (loginResult) {
                System.out.println("Anonymous login successful");
            } else {
                System.out.println("Anonymous login failed");
            }
            // 不加这个就会导致文件损坏
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            1. 下载的代码
//            String currentDirectory = new File(".").getAbsolutePath();
//            localFilePath = currentDirectory + localFilePath;
//            System.out.println("输出的文件路径是: " + localFilePath);
//            ftpClient.retrieveFile(remoteFilePath, new FileOutputStream(localFilePath));

            // 2. 直接读的代码，获取文件输入流
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("连接FTP服务器失败");
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout(); // 注销
                    ftpClient.disconnect(); // 断开连接
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void modifyBasketStatus(String basketRfid, String status) {
//        LambdaQueryWrapper<Basket> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(Basket::getBasketRfid, basketRfid);
        Basket basket = new Basket();
        basket.setBasketRfid(basketRfid);
        basket.setStatus(status);
        getBaseMapper().updateById(basket);
    }

    public void modifyBasketStatusBatch(List<String> basketList, String status, Long userId) {
        UpdateWrapper<Basket> updateWrapper = new UpdateWrapper<>();
        // 设置更新条件，这里使用 in 查询，即 WHERE basket_id IN (...)
        updateWrapper.in("basket_rfid", basketList);
        // 设置要更新的字段和值
        Basket basket = new Basket();
        basket.setStatus(status);
        basket.setUserId(userId);
        // 执行更新操作
        getBaseMapper().update(basket, updateWrapper);
    }

    // 门店接收订单
    @Override
    public void receiveOrder(Long orderId, Long ownerId) {
        // 鉴权
        NumberWithFormat userIdFormat = (NumberWithFormat) StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY);
        Long orderIdRes = orderService.queryOrderById(orderId).getStoreId();
        if (userIdFormat.longValue() != orderIdRes) {
            throw new BizException(BizResponseCode.ERR_11004, "越权操作，只能本人确认收货！");
        }

        // 更新筐子的位置
        String basketListStr = basketOrderService.getBasketList(orderId);
        Gson gson = new Gson();
        // 定义 List<String> 的类型
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        // 使用 Gson 将 JSON 字符串转换为 List<String>
        List<String> basketList = gson.fromJson(basketListStr, listType);
        // 更新框子的状态
        UpdateWrapper<Basket> updateWrapper = new UpdateWrapper<>();
        // 设置更新条件，这里使用 in 查询，即 WHERE basket_id IN (...)
        updateWrapper.in("basket_rfid", basketList);
        // 设置要更新的字段和值
        Basket basket = new Basket();
        basket.setStatus(Constants.Basket_Free_KEY);
        basket.setUserId(ownerId);
        // 执行更新操作
        getBaseMapper().update(basket, updateWrapper);
        orderService.updateDownTime(orderId);
    }
}
