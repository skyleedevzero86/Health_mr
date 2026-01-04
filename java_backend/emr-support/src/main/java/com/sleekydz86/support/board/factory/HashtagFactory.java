package com.sleekydz86.support.board.factory;

import com.sleekydz86.support.board.valueobject.Hashtag;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HashtagFactory {

    public List<Hashtag> createFromStrings(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Hashtag::of)
                .collect(Collectors.toList());
    }
}

