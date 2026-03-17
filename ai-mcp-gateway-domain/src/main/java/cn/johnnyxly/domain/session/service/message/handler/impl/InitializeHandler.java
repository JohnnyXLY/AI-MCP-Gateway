package cn.johnnyxly.domain.session.service.message.handler.impl;

import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;
import cn.johnnyxly.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 协议握手，建立客户端与服务器的连接
 */

@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {

    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {
        log.info("模拟处理初始化请求");

        return new McpSchemaVO.JSONRPCResponse(
                "2.0",
                message.id(),
                Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of()
                    ),
                "serverInfo", Map.of(
                        "name", "MCP Weather Proxy Server",
                        "version", "1.0.0"
                    )
                ),
                null
        );

        /* 完整 JSON 响应
         * {
         *   "jsonrpc": "2.0",
         *   "id": 123,
         *   "result": {
         *     "protocolVersion": "2024-11-05",
         *     "capabilities": {
         *       "tools": {},
         *       "resources": {}
         *     },
         *     "serverInfo": {
         *       "name": "MCP Weather Proxy Server",
         *       "version": "1.0.0"
         *     },
         *   "error": null
         *   }
         * }
         */
    }

}
