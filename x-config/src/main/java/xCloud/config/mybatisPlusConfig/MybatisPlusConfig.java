//package xCloud.config.mybatisPlusConfig;
//
//import com.baomidou.mybatisplus.annotation.DbType;
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @Description
// * @Author Andy Fan
// * @Date 2025/4/7 13:41
// * @ClassName MybatisPlusConfig
// *
// * PaginationInterceptor
// * 而不是
// * PaginationInnerInterceptor
// */
//@Configuration
//public class MybatisPlusConfig {
//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//// 添加分页插件--PaginationInnerInterceptor
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
//        return interceptor;
//    }
//}
