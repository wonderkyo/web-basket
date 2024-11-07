package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class queryBasketRequest implements Convert {
    private Long pageNo;

    private Long pageSize;

    // all-不分页查询，fenye-分页查询
    private String queryType;

    // 查询列名，可以不写
    private String queryColumn;

    // 查询条件，可以不写
    private String queryCodition;

    public <T> IPage<T> toPage() {
        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setMaxLimit(500L);
        page.setCurrent(pageNo);
        return page;
    }

}
