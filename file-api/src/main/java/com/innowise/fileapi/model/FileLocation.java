package com.innowise.fileapi.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@NoArgsConstructor
@ToString
@Getter
@AllArgsConstructor
public class FileLocation {

    @Id
    private String id;

    @Setter
    private Location location;

    @Setter
    private String key;


    public enum Location {
        S3,
        LOCAL,
        NONE
    }
}
