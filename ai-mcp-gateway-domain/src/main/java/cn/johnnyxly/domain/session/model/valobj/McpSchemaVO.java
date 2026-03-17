package cn.johnnyxly.domain.session.model.valobj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;

/**
 * MCP 架构值对象
 */
@Slf4j
public class McpSchemaVO {

    // TypeReference 是 Jackson 提供的一个工具类，核心作用是绕过类型擦除，保留泛型信息
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JSONRPCMessage deserializeJsonRpcMessage (String jsonText)
            throws IOException {

        log.debug("Received JSON message: {}", jsonText);

        // 将 json 字符串转换为 HashMap 格式
        // eg:
        // String jsonText = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":\"95835f74-0\"}
        // -->
        // {
        //  "jsonrpc": "2.0",
        //  "method": "initialize",
        //  "id": "95835f74-0",
        // }
        var map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

        // 判断消息是请求对象还是响应对象
        if (map.containsKey("method") && map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCRequest.class);
        } else if (map.containsKey("result") || map.containsKey("error")) {
            return objectMapper.convertValue(map, JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
    }

    // sealed 关键字修饰接口/类，意为「密封」，限制其实现类/子类的范围
    // permits 关键字紧跟 sealed，列出允许实现/继承的类名
    // JSONRPCMessage 接口只能被 JSONRPCRequest 和 JSONRPCResponse 实现
    // 避免出现其他非法的 MCP 消息类型，保证网关解析 MCP 消息时的安全性
    public sealed interface JSONRPCMessage permits JSONRPCRequest, JSONRPCResponse {
        String jsonrpc();
    }

    /**
     * 请求对象
     * @param jsonrpc 协议版本
     * @param method  请求方法：initialize、tools/list、tools/call、resources/list
     * @param id      请求ID
     * @param params  请求参数
     */
    // record 是 Java 用于定义「不可变数据载体类」的关键字
    // 替代传统的 class + private final 字段 + getter + equals + hashCode + toString 样板代码
    @JsonInclude(JsonInclude.Include.NON_ABSENT)  // 非空字段才序列化
    @JsonIgnoreProperties(ignoreUnknown = true)  // 忽略 JSON 中不存在的 Java 字段
    public record JSONRPCRequest(
            // @JsonProperty进行字段映射，绑定 Java 字段和 JSON 字段名
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("method") String method,
            @JsonProperty("id") Object id,
            @JsonProperty("params") Object params
    ) implements JSONRPCMessage {}

    /*
     * 完全等价的 class 实现
     * @JsonInclude(JsonInclude.Include.NON_ABSENT)
     * @JsonIgnoreProperties(ignoreUnknown = true)
     * public class JSONRPCRequestClass implements JSONRPCMessage {
     *
     *     // 对应 record 的字段，默认 private final（不可变）
     *     private final String jsonrpc;
     *     private final String method;
     *     private final Object id;
     *     private final Object params;
     *
     *     // record 自动生成的全参构造器
     *     public JSONRPCRequestClass(
     *             @JsonProperty("jsonrpc") String jsonrpc,
     *             @JsonProperty("method") String method,
     *             @JsonProperty("id") Object id,
     *             @JsonProperty("params") Object params) {
     *         this.jsonrpc = jsonrpc;
     *         this.method = method;
     *         this.id = id;
     *         this.params = params;
     *     }
     *
     *     // record 自动生成的 getter 方法（字段名() 形式）
     *     @JsonProperty("jsonrpc") // 注解要同步加在 getter 上，保证序列化/反序列化生效
     *     @Override
     *     public String jsonrpc() {
     *         return this.jsonrpc;
     *     }
     *
     *     @JsonProperty("method")
     *     public String method() {
     *         return this.method;
     *     }
     *
     *     @JsonProperty("id")
     *     public Object id() {
     *         return this.id;
     *     }
     *
     *     @JsonProperty("params")
     *     public Object params() {
     *         return this.params;
     *     }
     *
     *     // record 自动生成的 equals 方法
     *     @Override
     *     public boolean equals(Object o) {
     *         if (this == o) return true;
     *         if (o == null || getClass() != o.getClass()) return false;
     *         JSONRPCRequestClass that = (JSONRPCRequestClass) o;
     *         return Objects.equals(jsonrpc, that.jsonrpc) &&
     *                 Objects.equals(method, that.method) &&
     *                 Objects.equals(id, that.id) &&
     *                 Objects.equals(params, that.params);
     *     }
     *
     *     // record 自动生成的 hashCode 方法
     *     @Override
     *     public int hashCode() {
     *         return Objects.hash(jsonrpc, method, id, params);
     *     }
     *
     *     // record 自动生成的 toString 方法
     *     @Override
     *     public String toString() {
     *         return "JSONRPCRequestClass[" +
     *                 "jsonrpc=" + jsonrpc +
     *                 ", method=" + method +
     *                 ", id=" + id +
     *                 ", params=" + params +
     *                 ']';
     *     }
     * }
     */

    /**
     * 响应对象
     * @param jsonrpc 协议版本
     * @param id      请求ID
     * @param result  响应结果
     * @param error   异常结果
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JSONRPCResponse(@JsonProperty("jsonrpc") String jsonrpc,
                                 @JsonProperty("id") Object id,
                                 @JsonProperty("result") Object result,
                                 @JsonProperty("params") JSONRPCError error
    ) implements JSONRPCMessage {
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record JSONRPCError(
                @JsonProperty("code") int code,
                @JsonProperty("message") String message,
                @JsonProperty("data") Object data) {
        }
    }
}
