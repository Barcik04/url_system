package com.example.url_system.dtos.mappers;

import com.example.url_system.dtos.CreateResponseUrlDto;
import com.example.url_system.dtos.StatsUrlDto;
import com.example.url_system.dtos.UrlResponseDto;
import com.example.url_system.models.Url;
import org.springframework.data.domain.Page;

public interface UrlMapper {
    UrlResponseDto urlToDto(Url url);
    CreateResponseUrlDto urlToCreateDto(Url url);
    StatsUrlDto urlToStatsDto(Url url);

    Page<StatsUrlDto> urlToStatsPageDto(Page<Url> urls);

}
