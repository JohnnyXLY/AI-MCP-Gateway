package cn.johnnyxly.cases.mcp;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * MCP 会话服务接口
 */
public interface IMcpSessionService {

    /**
     * 建立 MCP 会话
     * @param gatewayId 网关ID
     * @return 流式响应
     * @throws Exception
     */
    Flux<ServerSentEvent<String>> createMcpSession(String gatewayId) throws Exception;

}
