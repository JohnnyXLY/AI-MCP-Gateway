package cn.johnnyxly.domain.session.service.message.handler;

import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;

/**
 * 处理请求接口
 */
public interface IRequestHandler {

    McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message);

}
