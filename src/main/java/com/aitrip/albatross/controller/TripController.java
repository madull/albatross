package com.aitrip.albatross.controller;

import cn.hutool.core.util.StrUtil;
import com.aitrip.albatross.client.TripStreamClient;
import com.aitrip.albatross.listener.TripEventSourceListener;
import com.aitrip.albatross.model.StreamMsg;
import com.unfbx.chatgpt.exception.BaseException;
import com.unfbx.chatgpt.exception.CommonError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
@Controller
@Slf4j
public class TripController {
    @Autowired
    private TripStreamClient tripStreamClient;

    @GetMapping("/chat")
    @CrossOrigin
    public SseEmitter chat(@RequestParam("message") String msg, @RequestHeader Map<String, String> headers) throws IOException {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        String uid = headers.get("uid");
        if (StrUtil.isBlank(uid)) {
            throw new BaseException(CommonError.SYS_ERROR);
        }

        sseEmitter.send(SseEmitter.event().id(uid).name("连接成功！！！！").data(LocalDateTime.now()).reconnectTime(3000));
        sseEmitter.onCompletion(() -> {
            log.info(LocalDateTime.now() + ", uid#" + uid + ", on completion");
        });
        sseEmitter.onTimeout(() -> log.info(LocalDateTime.now() + ", uid#" + uid + ", on timeout#" + sseEmitter.getTimeout()));
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info(LocalDateTime.now() + ", uid#" + "765431" + ", on error#" + throwable.toString());
                        sseEmitter.send(SseEmitter.event().id("765431").name("发生异常！").data(throwable.getMessage()).reconnectTime(3000));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        msg = getJson();

        TripEventSourceListener openAIEventSourceListener = new TripEventSourceListener(sseEmitter);
        tripStreamClient.streamCompletions(new StreamMsg(uid,"",msg), openAIEventSourceListener);
        return sseEmitter;
    }


    public String getJson(){
        String s = "我想去杭州玩3天，按照下面的json给我生成一样json格式的旅游规划 \n" +
                "[\n" +
                "  { \"day\":1,\n" +
                "    \"name\": \"厦门大学\",\n" +
                "    \"duration\": \"1-2h\",\n" +
                "    \"opening_hours\": \"上午\",\n" +
                "    \"ticket\": \"免费\",\n" +
                "    \"highlight\": \"拥有美丽的校园景色，包括芙蓉湖和世纪钟。\",\n" +
                "    \"restaurant\": \"老泉州餐厅（大众点评评分4.2分，特色菜：蚵仔煎）\",\n" +
                "    \"transportation\": \"从厦门大学步行约15分钟到南普陀\"\n" +
                "  },\n" +
                "  { \"day\":1,\n" +
                "    \"name\": \"南普陀寺\",\n" +
                "    \"duration\": \"2-3h\",\n" +
                "    \"opening_hours\": \"中午\",\n" +
                "    \"ticket\": \"免费\",\n" +
                "    \"highlight\": \"是一座著名的佛教寺庙，坐落在五老峰下，香火鼎盛。\",\n" +
                "    \"restaurant\": \"南普陀寺斋餐厅（素斋） 开元寺斋餐厅（大众点评评分4.0分，特色菜：莲花素糕）泉州美食城（大众点评评分4.1分，特色菜:土笋冻）\"\n" +
                "  },\n" +
                "  { \"day\":1,\n" +
                "    \"name\": \"沙坡尾\",\n" +
                "    \"duration\": \"2-3h\",\n" +
                "    \"opening_hours\": \"下午\",\n" +
                "    \"ticket\": \"免费\",\n" +
                "    \"highlight\": \"艺术西区是一个充满艺术氛围的地方，有很多咖啡馆、画廊和手工艺品店。\",\n" +
                "    \"restaurant\": \"沙坡尾排骨饭\",\n" +
                "    \"transportation\": \"步行或打车前往住宿地点\"\n" +
                "  }\n" +
                "]";
        return  s;
    }


}
