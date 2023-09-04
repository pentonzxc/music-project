package com.innowise.enricher.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.enricher.model.SongInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParseSpotifyMessage implements Handler<Tuple2<String, String>, SongInfo> {

    final ObjectMapper objectMapper;


    @Override
    public Flux<SongInfo> handle(Flux<Tuple2<String, String>> flux) {
        return flux.handle((tuple, sink) -> {
            String message = tuple.getT1();
            String songDatabaseId = tuple.getT2();

            try {
                SongInfo songInfo = parse(message);
                songInfo.setSongId(songDatabaseId);
                sink.next(songInfo);
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
            }
        });
    }


    private SongInfo parse(String message) throws JsonProcessingException {
//        Starting parse message
        JsonNode mainNode = objectMapper.readTree(message);

        JsonNode tracksNode = mainNode.get("tracks");
        JsonNode options = tracksNode.get("items");

        if (!options.isArray()) throw new IllegalArgumentException("Api is out of date");

//        Take first match , btw it isn't accurate to rely on that
//        TODO: make more accurate system to find track based on: name , content ???

        JsonNode mostPopularTrack = options.get(0);

        String name = mostPopularTrack.get("name").asText();
        String albumType = mostPopularTrack.get("album").get("album_type").asText();
        String releaseDate = mostPopularTrack.get("album").get("release_date").asText();
        List<String> artists = mostPopularTrack.get("artists").findValuesAsText("name");
        String durationInMillis = mostPopularTrack.get("duration_ms").asText();

        return SongInfo.builder()
                .name(name)
                .albumType(albumType)
                .releaseDate(releaseDate)
                .artists(artists)
                .durationInMillis(durationInMillis)
                .build();
    }
}
