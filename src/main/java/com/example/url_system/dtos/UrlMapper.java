package com.example.url_system.dtos;

import com.example.url_system.models.Url;
import org.springframework.data.domain.Page;

public interface UrlMapper {
    UrlResponseDto urlToDto(Url url);
    CreateResponseUrlDto urlToCreateDto(Url url);
    StatsUrlDto urlToStatsDto(Url url);

    Page<StatsUrlDto> urlToStatsPageDto(Page<Url> urls);
}
