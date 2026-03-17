package cn.johnnyxly.domain.session.service.message;

import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;
import cn.johnnyxly.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import cn.johnnyxly.domain.session.service.ISessionMessageService;
import cn.johnnyxly.domain.session.service.message.handler.IRequestHandler;
import cn.johnnyxly.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static cn.johnnyxly.types.enums.ResponseCode.METHOD_NOT_FOUND;

/**
 * 会话消息服务
 */

@Slf4j
@Service
public class SessionMessageService implements ISessionMessageService {

    // requestHandlerMap 是 Spring 容器通过自动注入的方式
    // 将所有实现了 IRequestHandler 接口且标注了 @Service（或 @Component 等）的 Bean 收集到一个 Map 中
    // 形成 String -> IRequestHandler 的映射
    @Resource
    private Map<String, IRequestHandler> requestHandlerMap;

    @Override
    public McpSchemaVO.JSONRPCResponse processHandlerMessage(McpSchemaVO.JSONRPCRequest request) {
        String method = request.method();
        log.info("开始处理请求，方法: {}", method);

        SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
        if (null == sessionMessageHandlerMethodEnum) {
            throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
        }

        String handlerName = sessionMessageHandlerMethodEnum.getHandlerName();
        IRequestHandler requestHandler = requestHandlerMap.get(handlerName);
        if (null == requestHandler) {
            throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
        }

        // 使用枚举策略模式处理请求
        return requestHandler.handle(request);
    }

}
