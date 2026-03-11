package cn.johnnyxly.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 会话对象配置
 * 包含会话ID、流式响应消息发送器、会话创建时间、会话最后访问时间、会话活跃状态
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionConfigVO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 流式响应消息发送器
     * ServerSentEvent<String> -> 推送的消息为字符串类型
     * Sinks.Many ->
     * Sinks：Spring WebFlux 提供的 “消息接收器 / 发送器”，可以理解为 “消息管道的入口”
     * Many：表示这个 Sinks 支持多生产者、多消费者（网关场景中，可能多个线程向同一个会话推送消息，多个客户端连接消费消息）
     */
    private Sinks.Many<ServerSentEvent<String>> sink;

    /**
     * 会话创建时间
     * Instant：表示时间线上的一个瞬时点，精确到纳秒，是Java中最精准、最推荐的时间表示方式，并且线程安全
     */
    private Instant createTime;

    /**
     * 会话最后访问时间
     * 网关内部有多个线程会对此变量进行监测（如消息推送线程、心跳监测线程、过期清理线程）
     * 要保证多线程下的可见性，因而使用volatile
     */
    private volatile Instant lastAccessTime;

    /**
     * 当前会话活跃状态
     * 非活跃状态的会话会被移除
     */
    private volatile boolean active;

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessTime = Instant.now();
        this.active = true;
    }

    /**
     * 标记为非活跃状态
     */
    public void markInactive() {
        this.active = false;
    }

    /**
     * 更新会话最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = Instant.now();
    }

    /**
     * 会话超时判断
     * @param timeoutMinutes 超时时间（会话访问后的保活时间）
     * @return 是否超时
     */
    public boolean isExpired(long timeoutMinutes) {
        return lastAccessTime.isBefore(Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES));
    }
}
