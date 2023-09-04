package com.innowise.enricher.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@NoArgsConstructor
@ToString
@Getter
public class FileLocation {

    @Id
    private String id;

    @Setter
    private Location location;

    @Setter
    private String key;


    public enum Location {
        S3,
        LOCAL
    }




}
