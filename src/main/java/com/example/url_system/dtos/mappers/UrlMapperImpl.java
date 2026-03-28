package com.example.url_system.dtos.mappers;

import com.example.url_system.dtos.CreateResponseUrlDto;
import com.example.url_system.dtos.StatsUrlDto;
import com.example.url_system.dtos.UrlResponseDto;
import com.example.url_system.models.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlMapperImpl implements UrlMapper {
    @Override
    public UrlResponseDto urlToDto(Url url) {
        if (url == null) {
            return null;
        }

        return new UrlResponseDto(
                url.getCode(),
                url.getCreatedAt()
        );
    }

    @Override
    public CreateResponseUrlDto urlToCreateDto(Url url) {
        if (url == null) {
            return null;
        }

        return new CreateResponseUrlDto(
                url.getLongUrl(),
                url.getCode(),
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }


    @Override
    public StatsUrlDto urlToStatsDto(Url url) {
        if (url == null) {
            return null;
        }

        return new StatsUrlDto(
                url.getLongUrl(),
                url.getCode(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClicks()
        );
    }


    @Override
    public Page<StatsUrlDto> urlToStatsPageDto(Page<Url> urls) {
        if (urls == null) {
            return null;
        }

        List<StatsUrlDto> statsUrlDtos = new ArrayList<>();

        for  (Url url : urls) {
            if (url == null) {
                return null;
            }

            statsUrlDtos.add(new StatsUrlDto(
                    url.getLongUrl(),
                    url.getCode(),
                    url.getCreatedAt(),
                    url.getExpiresAt(),
                    url.getClicks()
            ));
        }

        return new PageImpl<>(statsUrlDtos);
    }
}
