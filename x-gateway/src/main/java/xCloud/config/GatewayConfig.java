//package xCloud.config;
//
//import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
//import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
//import jakarta.annotation.PostConstruct;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.codec.ServerCodecConfigurer;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import org.springframework.web.reactive.result.view.ViewResolver;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.*;
//
///**
// * @Description SentinelGateway config
// * @Author Andy Fan
// * @Date 2025/2/27 17:55
// * @ClassName GatewayConfig
// */
//
//@Configuration
//public class GatewayConfig {
//
//    //配置视图解析器
//    private final List<ViewResolver> viewResolvers;
//
//    //配置编码解码器
//    private final ServerCodecConfigurer serverCodecConfigurer;
//
//    public GatewayConfig(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
//        this.viewResolvers = viewResolvers;
//        this.serverCodecConfigurer = serverCodecConfigurer;
//    }
//
//    //初始化一个限流的过滤器
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public GlobalFilter sentinelGatewayFilter() {
//        return new SentinelGatewayFilter();
//    }
//
//    //配置初始化的限流参数
//    @PostConstruct
//    public void initGatewayRules() {
//        Set<GatewayFlowRule> rules = new HashSet<>();
//
//        rules.add(
//                new GatewayFlowRule("x-product")//资源名称，对应路由ID
//                        .setCount(1)//限流阀值
//                        .setIntervalSec(1)//时间窗口，单位为秒，默认1s
//        );
//        GatewayRuleManager.loadRules(rules);
//    }
//
//    //配置限流的异常处理器
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public SentinelGatewayBlockExceptionHandler sentinelGatewayExceptionHandler() {
//        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
//    }
//
//    //自定义限流异常页面
//    public void initBlockHandlers() {
//        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
//            @Override
//            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
//                Map map = new HashMap();
//                map.put("code", 0);
//                map.put("message", "限流了");
//                return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(BodyInserters.fromObject(map));
//            }
//        };
//        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
//    }
//}
