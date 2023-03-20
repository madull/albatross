package com.aitrip.albatross.client;

import cn.hutool.http.ContentType;
import com.aitrip.albatross.model.StreamMsg;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.constant.OpenAIConst;
import com.unfbx.chatgpt.exception.BaseException;
import com.unfbx.chatgpt.exception.CommonError;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

import java.net.Proxy;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TripStreamClient {

    @Getter
    @NotNull
    private String apiKey;
    /**
     * 自定义api host使用builder的方式构造client
     */
    @Getter
    private String apiHost = OpenAIConst.OPENAI_HOST;

    @Getter
    private OkHttpClient okHttpClient;
    /**
     * 连接超时
     */
    @Getter
    private long connectTimeout;
    /**
     * 写超时
     */
    @Getter
    private long writeTimeout;
    /**
     * 读超时
     */
    @Getter
    private long readTimeout;
    /**
     * okhttp 代理
     */
    @Getter
    private Proxy proxy;

    /**
     * 创建OpenAiClient，自定义OkHttpClient
     *
     * @param apiKey         key
     * @param connectTimeout 连接超时时间 单位秒
     * @param writeTimeout   写超时 单位秒
     * @param readTimeout    超时 单位秒
     */
    public TripStreamClient(String apiHost,String apiKey, long connectTimeout, long writeTimeout, long readTimeout) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
        this.okHttpClient(connectTimeout, writeTimeout, readTimeout);
    }

    private void okHttpClient(long connectTimeout, long writeTimeout, long readTimeout) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        client.writeTimeout(writeTimeout, TimeUnit.SECONDS);
        client.readTimeout(readTimeout, TimeUnit.SECONDS);
        this.okHttpClient = client.build();
    }


    public void streamCompletions(StreamMsg msg, EventSourceListener eventSourceListener) {
        if (Objects.isNull(eventSourceListener)) {
            log.error("参数异常：EventSourceListener不能为空，可以参考：com.unfbx.chatgpt.sse.ConsoleEventSourceListener");
            throw new BaseException(CommonError.PARAM_ERROR);
        }

        try {
            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(msg);
            Request request = new Request.Builder()
                    .url(this.apiHost + "chatGPT/chat")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
                    //.header("Authorization", "Bearer " + this.apiKey)
                    .header("Content-Type", "text/event-stream;charset=UTF-8")
                    .header("uid", UUID.randomUUID().toString())
                    .build();
            //创建事件
            EventSource eventSource = factory.newEventSource(request, eventSourceListener);
        } catch (JsonProcessingException e) {
            log.error("请求参数解析异常：{}", e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error("请求参数解析异常：{}", e);
            e.printStackTrace();
        }
    }
}
