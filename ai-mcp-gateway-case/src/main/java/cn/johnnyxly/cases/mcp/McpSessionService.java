package cn.johnnyxly.cases.mcp;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.session.factory.DefaultMcpSessionFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * MCP 会话服务
 */

@Service
public class McpSessionService implements IMcpSessionService {

    @Resource
    private DefaultMcpSessionFactory defaultMcpSessionFactory;

    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId) throws Exception {
        StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler = defaultMcpSessionFactory.strategyHandler();
        return strategyHandler.apply(gatewayId, new DefaultMcpSessionFactory.DynamicContext());
    }

}
