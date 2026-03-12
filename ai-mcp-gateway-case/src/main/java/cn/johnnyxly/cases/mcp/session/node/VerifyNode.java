package cn.johnnyxly.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.AbstractMcpSessionSupport;
import cn.johnnyxly.cases.mcp.session.factory.DefaultMcpSessionFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * 鉴权核验 - （todo 这部分后续实现）
 */

@Service
public class VerifyNode extends AbstractMcpSessionSupport {

    @Resource
    private SessionNode sessionNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        // 路由至下一节点 sessionNode
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }

}
