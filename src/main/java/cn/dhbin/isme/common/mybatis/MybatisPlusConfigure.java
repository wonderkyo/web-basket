package cn.dhbin.isme.common.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus配置
 *
 * @author dhb
 */
// Configuration 注释标注当前类是一个Spring Boot配置类，项目在启动时自动读取加载配置文件中我们定义的Bean对象
// MapperScam 定义实体类的扫描路径，在本项目中路径为
@Configuration
@MapperScan("cn.dhbin.isme.*.mapper")
public class MybatisPlusConfigure {


    /**
     * 分页插件
     *
     * @return bean
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建一个拦截器对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        /**
         * 我们将 new 一个分页插件拦截器 PaginationInnerInterceptor 参数为数据库类型 MySQL,当然也可以写其他数据库类型，根据自己的数据库类型而定
         * 然后将此拦截器add添加到拦截器对象中即可生效
         * (这里演示MyBatis-Plus分页插件拦截器，其实还可以继续添加其他拦截器,我们可以继续 new ，继续add添加到拦截器对象中哦！)
         */
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }


}
