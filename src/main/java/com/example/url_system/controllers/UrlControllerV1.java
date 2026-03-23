package com.example.url_system.controllers;

import com.example.url_system.dtos.*;
import com.example.url_system.exceptions.ApiError;
import com.example.url_system.models.Url;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.UrlService;
import com.example.url_system.services.UserService;
import com.example.url_system.utils.dynamicFiltering.UrlFilter;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;


@Tag(
        name = "Urls",
        description = "Endpoint for creating, redirecting, getting url stats, showing urls"
)
@RestController
@Validated
public class UrlControllerV1 {
    private final UrlService urlService;
    private final UserRepository userRepository;
    private final UserService userService;

    public UrlControllerV1(UrlService urlService, UserRepository userRepository, UserService userService) {
        this.urlService = urlService;
        this.userRepository = userRepository;
        this.userService = userService;
    }



    @Operation(
            summary = "Creates url and optionally links it to authenticated user",
            tags = {"Create"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "CreateResponseUrlDto of created url",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateResponseUrlDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "Url expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing Idempotency-Key",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request is already being processed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateResponseUrlDto> create(
            @Valid @RequestBody CreateUrlRequest  createUrlRequest,
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Idempotency-Key");
        }


        if (principal == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(urlService.create(createUrlRequest,  null, idempotencyKey));
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.create(createUrlRequest,  userId, idempotencyKey));
    }




    @Operation(summary = "Retrieving and redirecting by found code and register click",tags = {"register and click"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "url found"
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "url expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{code}")
    public ResponseEntity<Void> getUrlAndRegisterClick(@PathVariable String code) {
        Url url = urlService.getUrlAndRegisterClick(code);

        return ResponseEntity
                .status(302)
                .location(URI.create(url.getLongUrl()))
                .build();
    }





    @Operation(summary = "Display stats of found url",tags = {"stats"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "StatsUrlDto of found url",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatsUrlDto.class)
                    ))
    })
    @GetMapping("/api/v1/stats/{code}")
    @PreAuthorize("hasAuthority('USER')")
    public StatsUrlDto getStats(@PathVariable String code, @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        return urlService.getStatsUrl(code,  userId);
    }




    @Operation(summary = "Display all urls in the database paged",tags = {"Get All links (ADMIN)"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of found urls"
            )
    })
    @PostMapping("/api/v1/all-urls")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<Url> getAllLinks(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestBody(required = false) UrlFilter filter,
            @AuthenticationPrincipal UserDetails principal
            ) {

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 50),
                pageable.getSort()
        );

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        return urlService.getAllLinks(safePageable, filter, userId);
    }






    @Operation(summary = "Retrieving all urls linked to authenticated user Paged",tags = {"show my links"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of user's urls"
            )
    })
    @PostMapping("/api/v1/show-my-links")
    @PreAuthorize("hasAuthority('USER')")
    public Page<StatsUrlDto> getMyLinks(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) UrlFilter filter
    ) {
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 50),
                pageable.getSort()
        );

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        return urlService.showMyLinks(safePageable, userId, filter);
    }




    @Operation(summary = "Deleting Url by id",tags = {"delete url"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "url deleted"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Url doesnt belong to User",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/api/v1/delete-url/{code}")
    @PreAuthorize("hasAuthority('USER')")
    public void delete(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        urlService.deleteUrl(userId, code);
    }



    @DeleteMapping("api/v1/delete-account")
    public void deleteAcc(@RequestBody DeleteAccountRequest req, @AuthenticationPrincipal UserDetails principal) throws StripeException {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        userService.deleteAccount(userId, req.password());
    }



    @PatchMapping("api/v1/patch-url/{code}")
    public UrlResponseDto patchUrl(@AuthenticationPrincipal UserDetails principal, @PathVariable String code, @RequestBody PatchUrlDto patchUrlDto) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        return urlService.patchUrl(userId, code, patchUrlDto);
    }
}
