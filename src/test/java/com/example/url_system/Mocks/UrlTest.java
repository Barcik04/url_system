package com.example.url_system.Mocks;


import com.example.url_system.controllers.UrlControllerV1;
import com.example.url_system.dtos.CreateUrlRequest;
import com.example.url_system.jwt.AuthEntryPointJwt;
import com.example.url_system.jwt.AuthTokenFilter;
import com.example.url_system.jwt.JwtUtils;
import com.example.url_system.models.Url;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.security.SecurityConfig;
import com.example.url_system.services.UrlService;
import com.example.url_system.utils.ratelimit.RateLimitFilter;
import com.example.url_system.utils.ratelimit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(SecurityConfig.class)
@WebMvcTest(controllers = UrlControllerV1.class, properties = "ratelimit.enabled=true")
@ActiveProfiles("test")
class UrlTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;


    @MockitoBean UrlService urlService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean UrlRepository urlRepository;

    @MockitoBean
    AuthEntryPointJwt authEntryPointJwt;
    @MockitoBean AuthTokenFilter authTokenFilter;



    @MockitoBean RateLimitFilter rateLimitFilter;
    @MockitoBean RateLimitService rateLimitService;


    @MockitoBean JwtUtils jwtUtils;
    @MockitoBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @BeforeEach
    void allowRequestsThroughRateLimitFilter() throws Exception {
        doAnswer(inv -> {
            var req = inv.getArgument(0, jakarta.servlet.ServletRequest.class);
            var res = inv.getArgument(1, jakarta.servlet.ServletResponse.class);
            var chain = inv.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());
    }

    @BeforeEach
    void allowRequestsThroughFilters() throws Exception {

        doAnswer(inv -> {
            var req = inv.getArgument(0, jakarta.servlet.ServletRequest.class);
            var res = inv.getArgument(1, jakarta.servlet.ServletResponse.class);
            var chain = inv.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());

        doAnswer(inv -> {
            var req = inv.getArgument(0, jakarta.servlet.ServletRequest.class);
            var res = inv.getArgument(1, jakarta.servlet.ServletResponse.class);
            var chain = inv.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(req, res);
            return null;
        }).when(authTokenFilter).doFilter(any(), any(), any());
    }


    @Test
    void should_return_201_when_create_url() throws Exception {
        var createUrlRequest = new CreateUrlRequest("https://...", null);


        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUrlRequest)))
                .andExpect(status().isCreated());

    }


    @Test
    void should_response_302_when_register_url() throws Exception {
        var url = new Url();
        url.setLongUrl("https://www.youtube.com/watch?v=cC8QMrNWcFY&list=RDtalRDCCgN9Q&index=3");

        when(urlService.getUrlAndRegisterClick("iwa11"))
                .thenReturn(url);

        mockMvc.perform(get("/iwa11"))
                .andExpect(status().isFound());

        verify(urlService, times(1)).getUrlAndRegisterClick("iwa11");
    }



    @Test
    @WithMockUser(username = "igor", authorities = {"USER"})
    void should_return_200_when_get_stats_url() throws Exception {
        String username = "igor";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/stats/" + username))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).findByUsername(username);
    }




    @Test
    @WithMockUser(username = "igor", authorities = {"USER"})
    void should_return_401_when_user_not_authenticated_and_get_stats_url() throws Exception {

        when(userRepository.findByUsername("igor")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stats/awd"))
                .andExpect(status().isUnauthorized());

        verify(userRepository, times(1)).findByUsername("igor");
        verifyNoInteractions(urlService);
    }




    @Test
    @WithMockUser(username = "igor", authorities = {"ADMIN"})
    void should_return_200_when_get_all_link() throws Exception {
        mockMvc.perform(get("/api/v1/urls")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "igor", authorities = {"USER"})
    void should_return_403_when_get_all_link_and_user() throws Exception {
        mockMvc.perform(get("/api/v1/urls")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isForbidden());
    }
}

