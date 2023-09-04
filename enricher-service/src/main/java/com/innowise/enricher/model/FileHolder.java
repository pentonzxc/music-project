package com.innowise.enricher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;
import java.util.Map;


@Data
@AllArgsConstructor
public class FileHolder {

    byte[] content;

    Map<String, String> metadata;

    String id;
}
