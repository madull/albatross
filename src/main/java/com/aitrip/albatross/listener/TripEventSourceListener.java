package com.aitrip.albatross.listener;

import cn.hutool.json.JSONUtil;
import com.aitrip.albatross.model.DayTrip;
import com.aitrip.albatross.model.StreamContent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

/**
 * 描述：OpenAIEventSourceListener
 *
 * @author https:www.unfbx.com
 * @date 2023-02-22
 */
@Slf4j
public class TripEventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    private StringBuffer sb = new StringBuffer();

    public TripEventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]")
                    .reconnectTime(3000));
            return;
        }


        try {

            StreamContent streamContent = JSONUtil.toBean(data, StreamContent.class);
            String content = streamContent.getContent();


            if(content == null){
                System.out.printf(content);

            }else if(content.indexOf("[")>=0 || content.indexOf("]")>=0){


            }else if(content != null &&content.indexOf("}")>0){

                sb.append(streamContent.getContent().replace("},","}"));

                DayTrip dayTrip = JSONUtil.toBean(sb.toString(), DayTrip.class);
                log.info("####### data:{}",JSONUtil.toJsonStr(dayTrip));


                sseEmitter.send(SseEmitter.event()
                        .data(JSONUtil.toJsonStr(dayTrip))
                        .reconnectTime(3000));
                sb  = new StringBuffer();
            }else {
                sb.append(streamContent.getContent());

            }
        }catch (Exception e){
            e.printStackTrace();

        }


    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }



}
