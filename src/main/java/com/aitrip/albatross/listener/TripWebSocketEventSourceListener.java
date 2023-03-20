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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TripWebSocketEventSourceListener extends EventSourceListener {

    private StringBuffer sb = new StringBuffer();

    private Session session;

    public TripWebSocketEventSourceListener(Session session) {
        this.session = session;
    }

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
            session.getBasicRemote().sendText(data);
            return;
        }




        try {
            StreamContent streamContent = JSONUtil.toBean(data, StreamContent.class);
            String content = streamContent.getContent();


            if(content == null){

            }else if(content.indexOf("[")>=0 ){


            }else if(content.indexOf("]")>=0){


            }else if(content != null &&content.indexOf("}")>0){

                sb.append(streamContent.getContent().replace("},","}"));

                DayTrip dayTrip = JSONUtil.toBean(sb.toString(), DayTrip.class);
                //酒店
                String locationImg = parseHtml(dayTrip.getName());
                dayTrip.setLocationImg(locationImg);
                //美食

                String restaurant = dayTrip.getRestaurant();
                String[] split1 = restaurant.split("特色菜：");
                if(split1.length>1){
                    List<String> foods = new ArrayList<>();
                    String[] split = split1[1].split("、");
                    for(String f:split){
                        String fg = parseHtml(f);
                        foods.add(fg);
                    }
                    dayTrip.setFoods(foods);

                }







                session.getBasicRemote().sendText(JSONUtil.toJsonPrettyStr(dayTrip));
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
        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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






    String  parseHtml(String keyWord){

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
        try {
            String searchUrl = "https://cn.bing.com/images/search?q=" + keyWord + "&count=1&first=1&FORM=IBASEP";
            Document document = Jsoup.connect(searchUrl).userAgent(userAgent).get();

            Element imgTag = document.select("a.iusc").first();
            String imgUrl = imgTag.attr("href");

            return "https://cn.bing.com"+imgUrl;

        }catch ( Exception e){
            e.printStackTrace();

        }

        return "";
    }

    public static void main(String[] args) {
        String s = "海口市美食街（大众点评评分4.2分，特色菜：海南粉、椰子鸡）";
        String[] split = s.split("特色菜：");
        System.out.printf("");

    }

}
