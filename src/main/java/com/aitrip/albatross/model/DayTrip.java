package com.aitrip.albatross.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DayTrip implements Serializable {

    //{ "day":1,
    //    "name": "西湖",
    //    "duration": "2-3h",
    //    "opening_hours": "上午",
    //    "ticket": "免费",
    //    "highlight": "杭州的标志性景点，有美丽的湖泊和园林，可以划船游览。",
    //    "restaurant": "柳浪闻莺（大众点评评分4.4分，特色菜：西湖醋鱼）",
    //    "transportation": "从住宿地点乘坐公交车到西湖"
    //  }

    String day;
    String name;
    String duration;
    String opening_hours;
    String ticket;
    String highlight;
    String restaurant;
    String transportation;
    String hotel;
    String locationImg;
    List<String> foods;


}

