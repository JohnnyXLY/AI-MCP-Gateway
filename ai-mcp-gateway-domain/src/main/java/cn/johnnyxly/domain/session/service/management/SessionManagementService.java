package cn.johnnyxly.domain.session.service.management;

import cn.johnnyxly.domain.session.model.valobj.SessionConfigVO;
import cn.johnnyxly.domain.session.service.ISessionManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务
 */

@Slf4j
@Service
public class SessionManagementService implements ISessionManagementService {

    // 会话超时时间
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    // 当前活跃会话，ConcurrentHashMap存储保证线程安全，key值为sessionId
    private final Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>();

    // 定时任务调度
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public SessionManagementService() {
        // 把 cleanupExpiredSession 方法封装成 Runnable 接口实现类，传给定时调度器
        // 定时调度器开始计时，初始延迟 5 分钟开始执行首次任务
        // 第一次任务执行完成后，每 5 分钟（上一次开始执行后隔 5 分钟），自动再次执行 cleanupExpiredSession()
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredSession, 5, 5, TimeUnit.MINUTES);
        log.info("会话管理服务已启动，会话超时时间: {} 分钟", SESSION_TIMEOUT_MINUTES);
    }

    @Override
    public SessionConfigVO createSession(String gatewayId) {
        log.info("创建会话 gatewayId:{}", gatewayId);

        // 创建会话需要调用SessionConfigVO中的构造方法
        // 需要构建sessionId和sink
        String sessionId = UUID.randomUUID().toString();

        // 创建一个支持多消费者、带背压缓冲的 SSE 消息发送器
        // many -> 可发送多条消息
        // multicast -> 多播，支持多个消费者
        // onBackpressureBuffer -> 当生产者发消息速度超过消费者处理速度时，把暂时发不出去的消息先存入内存缓冲区，等消费者有能力处理时再发送
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 发送端点消息 -> 告知客户端消息请求地址（客户端第二次会使用 messageEndpoint 进行请求会话）
        String messageEndpoint = "/api-gateway/" + gatewayId + "/mcp/sse?sessionId=" + sessionId;
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("endpoint")
                .data(messageEndpoint)
                .build());

        SessionConfigVO sessionConfigVO = new SessionConfigVO(sessionId, sink);
        activeSessions.put(sessionId, sessionConfigVO);

        log.info("创建会话 gatewayId:{} sessionId:{},当前活跃会话数:{}", gatewayId, sessionId, activeSessions.size());
        return sessionConfigVO;
    }

    @Override
    public void removeSession(String sessionId) {
        log.info("删除会话配置 sessionId:{}", sessionId);

        // 删除会话需要进行的操作
        // 1. 从当前活跃列表中删除
        // 2. 将当前会话标记为非活跃状态，便于后续清理
        // 3. 当前会话进行收尾工作
        SessionConfigVO sessionConfigVO = activeSessions.remove(sessionId);

        if (sessionConfigVO == null) {
            return;
        }

        sessionConfigVO.markInactive();

        try {
            // 结束消息推送
            sessionConfigVO.getSink().tryEmitComplete();
        } catch (Exception e) {
            log.warn("关闭会话Sink时出错:{}", e.getMessage());
        }

        log.info("移除会话:{},剩余活跃会话数:{}", sessionId, activeSessions.size());
    }

    @Override
    public SessionConfigVO getSession(String sessionId) {
        // 获取会话执行的操作
        // 1. 判断会话是否存在
        // 2. 判断会话是否为活跃态
        // 3. 如果会话活跃，则更新会话最后访问时间
        if (null == sessionId || sessionId.isEmpty()) {
            return null;
        }

        SessionConfigVO sessionConfigVO = activeSessions.get(sessionId);

        if (null != sessionConfigVO && sessionConfigVO.isActive()) {
            sessionConfigVO.updateLastAccessedTime();
            return sessionConfigVO;
        }

        return null;
    }

    @Override
    public void cleanupExpiredSession() {
        int cleanedCount = 0;

        // 清理过期会话执行的操作
        // 1. 找出所有的待删除会话（非活跃或超时）
        // 2. 执行删除
        for (Map.Entry<String, SessionConfigVO> entry : activeSessions.entrySet()) {
            // 获取会话
            SessionConfigVO sessionConfigVO = entry.getValue();

            // 删除的条件：非活跃态或超时
            if (!sessionConfigVO.isActive() || sessionConfigVO.isExpired(SESSION_TIMEOUT_MINUTES)) {
                removeSession(sessionConfigVO.getSessionId());
                cleanedCount ++ ;
            }
        }

        if (cleanedCount > 0) {
            log.info("清理了 {} 个过期会话，剩余活跃会话数: {}", cleanedCount, activeSessions.size());
        }
    }

    @Override
    public void shutdown() {
        log.info("关闭会话管理服务...");

        // 关闭会话管理服务执行的操作
        // 1. 移除当前所有会话
        // 2. 清理调度器（收尾工作）
        for (String sessionId : activeSessions.keySet()) {
            removeSession(sessionId);
        }

        cleanupScheduler.shutdown();

        try {
            // 等待5秒让正在执行的任务完成
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                // 超时强制关闭
                cleanupScheduler.shutdown();
            }
        } catch (InterruptedException e) {
            // 异常强制关闭
            cleanupScheduler.shutdown();
            Thread.currentThread().interrupt();
        }

        log.info("关闭会话管理服务完成");
    }
}
