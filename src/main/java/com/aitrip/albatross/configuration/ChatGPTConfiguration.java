package com.aitrip.albatross.configuration;

import com.aitrip.albatross.client.TripStreamClient;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;

@Configuration
public class ChatGPTConfiguration {

    @Value("${apiKey}")
    private String apiKey;
    @Value("${apiHost}")
    private String apiHost;


    @Bean
    public TripStreamClient tripStreamClient() {
        TripStreamClient tripStreamClient = new TripStreamClient(apiHost, apiKey, 300, 300, 300);
        return tripStreamClient;
    }



}
