package cn.johnnyxly.domain.session.service;

import cn.johnnyxly.domain.session.model.valobj.McpSchemaVO;

/**
 * 会话消息接口
 */
public interface ISessionMessageService {

    McpSchemaVO.JSONRPCResponse processHandlerMessage(McpSchemaVO.JSONRPCRequest request);

}
