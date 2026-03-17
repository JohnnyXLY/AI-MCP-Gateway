package cn.johnnyxly.domain.session.service.message.handler.impl;

import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;
import cn.johnnyxly.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 执行指定的工具调用
 */

@Slf4j
@Service("toolsCallHandler")
public class ToolsCallHandler implements IRequestHandler {

    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {
        return null;
    }

}
