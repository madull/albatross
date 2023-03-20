package com.aitrip.albatross.websocket;

import com.aitrip.albatross.client.TripStreamClient;
import com.aitrip.albatross.listener.TripWebSocketEventSourceListener;
import com.aitrip.albatross.model.StreamMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 前后端交互的类实现消息的接收推送(自己发送给自己)
 *
 * @ServerEndpoint(value = "/test/one") 前端通过此URI和后端交互，建立连接
 */
@Slf4j
@ServerEndpoint(value = "/ws/trip")
@Component
public class TripWebSocket {



    /**
     * 记录当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /** 存放所有在线的客户端 */
    private static Map<String, Session> clients = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        onlineCount.incrementAndGet(); // 在线数加1
        clients.put(session.getId(), session);
        log.info("有新连接加入：{}，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        onlineCount.decrementAndGet(); // 在线数减1
        clients.remove(session.getId());
        log.info("有一连接关闭：{}，当前在线人数为：{}", session.getId(), onlineCount.get());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        TripWebSocketEventSourceListener tripWebSocketEventSourceListener = new TripWebSocketEventSourceListener(session);
        TripStreamClient tripStreamClient = SpringContext.getBean(TripStreamClient.class);
        String[] split = message.split(",");
        message = getJson(split[0],split[1],split[2]);
        tripStreamClient.streamCompletions(new StreamMsg(UUID.randomUUID().toString(),"",message), tripWebSocketEventSourceListener);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    public String getJson(String city,String days,String fee){
        String s = "我想去"+city+"玩"+days+"天，按照下面的json给我生成一样json格式的旅游规划,每天都需要酒店 \n" +
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
                "    \"transportation\": \"步行或打车前往住宿地点\",\n" +
                "    \"hotel\": \"泉州晋江喜来登酒店,+86 595 85188888,泉州市晋江区阳光路166号,500-700元/晚。\"\n" +
                "  }\n" +
                "]";

        return  s;
    }

}
