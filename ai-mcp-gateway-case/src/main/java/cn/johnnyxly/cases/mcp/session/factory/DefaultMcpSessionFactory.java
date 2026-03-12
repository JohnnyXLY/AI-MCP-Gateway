package cn.johnnyxly.cases.mcp.session.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.johnnyxly.cases.mcp.session.node.RootNode;
import cn.johnnyxly.domain.session.model.valobj.SessionConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * MCP 会话服务工厂
 */

@Service
public class DefaultMcpSessionFactory {

    @Resource
    private RootNode rootNode;

    // rootNode 根结点实现了 StrategyHandler 接口
    public StrategyHandler<String, DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private SessionConfigVO sessionConfigVO;
    }

}
