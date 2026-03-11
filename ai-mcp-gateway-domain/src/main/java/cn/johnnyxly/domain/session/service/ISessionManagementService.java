package cn.johnnyxly.domain.session.service;

import cn.johnnyxly.domain.session.model.valobj.SessionConfigVO;

/**
 * 会话管理接口
 */
public interface ISessionManagementService {
    /**
     * 创建会话
     * @param gatewayId 网关ID
     * @return 会话对象配置
     */
    SessionConfigVO createSession(String gatewayId);

    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    void removeSession(String sessionId);

    /**
     * 获取会话
     * @param sessionId 会话ID
     * @return 会话对象配置
     */
    SessionConfigVO getSession(String sessionId);

    /**
     * 清理过期会话
     */
    void cleanupExpiredSession();

    /**
     * 关闭会话服务并清理相关资源
     */
    void shutdown();
}
