package cn.johnnyxly.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.AbstractMcpSessionSupport;
import cn.johnnyxly.cases.mcp.session.factory.DefaultMcpSessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * 根节点
 */

@Slf4j
@Service
public class RootNode extends AbstractMcpSessionSupport {

    @Resource
    private VerifyNode verifyNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("创建会话 mcp session RootNode:{}", requestParameter);

            // 路由至下一节点 verifyNode
            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("创建会话 mcp session RootNode 异常:{}", requestParameter, e);
            throw e;
        }
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return verifyNode;
    }

}
