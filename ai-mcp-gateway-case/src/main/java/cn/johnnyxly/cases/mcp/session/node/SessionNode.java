package cn.johnnyxly.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.AbstractMcpSessionSupport;
import cn.johnnyxly.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.johnnyxly.domain.session.model.valobj.SessionConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * 会话节点
 */

@Slf4j
@Service
public class SessionNode extends AbstractMcpSessionSupport {

    @Resource
    private EndNode endNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("创建会话-SessionNode:{}", requestParameter);

        // 创建会话服务
        SessionConfigVO sessionConfigVO = sessionManagementService.createSession(requestParameter);

        // 写入上下文
        dynamicContext.setSessionConfigVO(sessionConfigVO);

        // 路由至下一节点 endNode
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }

}
