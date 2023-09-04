package com.innowise.songapi.model;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Data
@ToString
public class SongInfo implements Serializable {

    String songId;

    String name;

    String albumType;

    String releaseDate;

    List<String> artists;

    String durationInMillis;

}
