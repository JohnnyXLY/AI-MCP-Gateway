package cn.johnnyxly.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * MCP 网关服务接口
 */
public interface IMcpGatewayService {

    /**
     * 建立 SSE 连接
     * @param gatewayId 网关ID
     * @return 流式响应
     * @throws Exception
     */
    Flux<ServerSentEvent<String>> establishSSEConnection(String gatewayId) throws Exception;
}
