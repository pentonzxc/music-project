package com.innowise.enricher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@ToString
public class SongInfo {

    String songId;

    String name;

    String albumType;

    String releaseDate;

    List<String> artists;

    String durationInMillis;
}
