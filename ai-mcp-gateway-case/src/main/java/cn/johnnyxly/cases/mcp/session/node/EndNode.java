package cn.johnnyxly.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.AbstractMcpSessionSupport;
import cn.johnnyxly.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.johnnyxly.domain.session.model.valobj.SessionConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * 结束节点
 */

@Slf4j
@Service
public class EndNode extends AbstractMcpSessionSupport {

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("创建会话-EndNode:{}", requestParameter);

        // 获取上下文
        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        String sessionId = sessionConfigVO.getSessionId();

        Sinks.Many<ServerSentEvent<String>> sink = sessionConfigVO.getSink();

        // 响应式代码是「声明式」的逻辑定义（告诉框架 “要做什么”），而非「命令式」的执行步骤（告诉框架 “什么时候做”）
        // 状态切换和退出不是由代码里的某一行触发，而是由「SSE 连接的生命周期」和「Reactor 流的订阅机制」驱动
        // 这里只是列举了可能的情况已经对应的解决方案
        return sink.asFlux()  // 将Sinks.Many（消息发送器）转换成Flux流
                .mergeWith(
                        // 心跳机制
                        // 每 60 秒 ping 一次保活
                        Flux.interval(Duration.ofSeconds(60))
                                .map(i -> ServerSentEvent.<String>builder()
                                        .event("ping")
                                        .data("ping")
                                        .build())
                )
                // 连接取消时的清理逻辑
                .doOnCancel(() -> {
                    log.info("SSE连接取消，会话ID: {}", sessionId);
                    sessionManagementService.removeSession(sessionId);
                })
                // 连接终止时的清理逻辑
                .doOnTerminate(() -> {
                    log.info("SSE连接终止，会话ID: {}", sessionId);
                    sessionManagementService.removeSession(sessionId);
                });
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

}
