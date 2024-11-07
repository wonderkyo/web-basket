package cn.dhbin.isme.pms.util;

public class Constants {
    // 刚创建完
    public static final String ORDER_CREATE = "create";

    // 仓库已接单，待揽件
    public static final String ORDER_WARE_ACP = "wareAcp";

    // 司机已接单
    public static final String ORDER_DRIVER_ACP = "driverAcp";

    // 司机来了之后，仓库已出库，等待开始运输
    public static final String ORDER_OUT_WARE = "outWare";

    // 司机开始运输（运输中）
    public static final String ORDER_DRIVER_BEGIN = "driverBegin";

    // 司机已到达，待门店收货
    public static final String ORDER_DRIVER_ARRIVE = "driveArrive";

    // 门店已收货
    public static final String ORDER_STORE_ACP = "storeAcp";

    // 角色
    public static final String ROLE_STORE = "store";
    public static final String ROLE_DRIVER = "driver";
    public static final String ROLE_DEPOT = "depot";
    public static final String ROLE_ADMIN = "admin";

    public static final String Basket_Occupied_KEY = "2";
    public static final String Basket_Free_KEY = "1";
    public static final String Basket_Broken = "3";
    public static final String Basket_Lost = "5";
    public static final String Basket_RETURN = "4";
}

