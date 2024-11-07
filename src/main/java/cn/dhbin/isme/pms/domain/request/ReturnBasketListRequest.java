package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnBasketListRequest implements Convert {
    @NotNull(message = "查询页码不能为空")
    private Long pageNo;

    @NotNull(message = "每页显示条数不能为空")
    private Long pageSize;

    public <T> IPage<T> toPage() {
        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setMaxLimit(500L);
        page.setCurrent(pageNo);
        return page;
    }


}