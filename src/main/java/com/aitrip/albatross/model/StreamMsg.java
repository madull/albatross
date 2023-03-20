package com.aitrip.albatross.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StreamMsg implements Serializable {

    private String uid;
    private String role;
    private String msg;
}
