
package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class QueryMyBasketRequest implements Convert {
    private Long queryId; // 一般是不传的，只给管理员查询这个门店/仓库周转筐情况的时候传
    private Long pageNo;

    private Long pageSize;

    private String queryStatus;

    private Long startTime;

    private Long endTime;
    public <T> IPage<T> toPage() {
        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setMaxLimit(500L);
        page.setCurrent(pageNo);
        return page;
    }

}
