package cn.johnnyxly.trigger.http;

import cn.johnnyxly.api.IMcpGatewayService;
import cn.johnnyxly.cases.mcp.IMcpSessionService;
import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;
import cn.johnnyxly.domain.session.service.ISessionMessageService;
import cn.johnnyxly.types.enums.ResponseCode;
import cn.johnnyxly.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;

/**
 * MCP 网关服务接口管理
 */

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/")
public class McpGatewayController implements IMcpGatewayService {

    @Resource
    private IMcpSessionService mcpSessionService;

    @Resource
    private ISessionMessageService serviceMessageService;


    public McpGatewayController() {
        log.info("McpGatewayController");
    }

    /**
     * 建立 SSE 连接，创建会话
     * @param gatewayId 网关ID
     * @return
     * @throws Exception
     */
    @Override
    @GetMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> establishSSEConnection(@PathVariable("gatewayId") String gatewayId) throws Exception {
        try {
            log.info("建立 MCP SSE 连接，gatewayId:{}", gatewayId);
            if (StringUtils.isBlank(gatewayId)) {
                log.info("非法参数，gateway is null");
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            return mcpSessionService.createMcpSession(gatewayId);
        } catch (Exception e) {
            log.error("建立 MCP SSE 连接失败，gatewayId: {}", gatewayId, e);
            throw e;
        }
    }

    /**
     * 处理 SSE 消息，转换为通用形式(JSON-RPC)，响应会话
     * @param gatewayId 网关ID
     * @param sessionId 会话ID
     * @param messageBody 消息体
     * @return
     */
    @Override
    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> handleMessage(@PathVariable("gatewayId") String gatewayId,
                                                      @RequestParam String sessionId,
                                                      @RequestBody String messageBody) {
        try {
            log.info("处理 MCP SSE 消息，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);
            McpSchemaVO.JSONRPCMessage jsonrpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
            log.info("序列化消息:{}", jsonrpcMessage.jsonrpc());

            McpSchemaVO.JSONRPCResponse jsonrpcResponse = serviceMessageService.processHandlerMessage((McpSchemaVO.JSONRPCRequest) jsonrpcMessage);
            log.info("调用结果:{}", JSON.toJSONString(jsonrpcResponse));

            // Mono 是 Spring WebFlux（响应式编程框架）中的核心类，代表「0 或 1 个元素的异步序列」
            // Mono.just() 创建包含一个元素的序列
            // Spring 处理 HTTP 响应时，会通过 HttpMessageConverter（消息转换器）
            // 把 Java 对象（比如 Map、实体类）转换成客户端能识别的格式（比如 JSON、XML）
            // Spring 会自动把 ResponseEntity 中存储的 Map 序列化成 JSON 格式，返回给客户端
            // 客户端收到的响应体就是：
            // {
            //  "status": "sent via SSE"
            // }
            return Mono.just(ResponseEntity.ok(Map.of("status", "sent via SSE")));
        } catch (Exception e) {
            log.info("处理 MCP SSE 消息失败，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody, e);
            // 返回空序列
            return Mono.empty();
        }
    }
}
