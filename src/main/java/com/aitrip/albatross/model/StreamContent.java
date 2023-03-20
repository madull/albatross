package com.aitrip.albatross.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class StreamContent implements Serializable {
    private String role;
    private String content;
}
