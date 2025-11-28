package com.example.demo.service.github;

import com.example.demo.dto.repository.GithubRepositoryResponseDto;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.model.file.FileEntity;
import com.example.demo.model.user.UserEntity;
import com.example.demo.service.file.FileUtilService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {

    private final FileUtilService fileUtilService;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestClient restClient;

    public FileEntity downloadAndSaveRepoToZip(String repoName) {
        String repoDownloadUri = "https://api.github.com/repos/%s/%s/zipball/main";

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        UserEntity user = userService.getCurrentUser();
        String accessToken = client.getAccessToken().getTokenValue();
        String repoDownloadUriFormatted = String.format(repoDownloadUri, user.getUsername(), repoName);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set(HttpHeaders.ACCEPT, "application/zip");

        ResponseEntity<byte[]> response = restClient.get()
                .uri(repoDownloadUriFormatted)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(byte[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new ResourceNotFoundException("Resource not found");
        }

        return fileUtilService.uploadFile(
                new ByteArrayInputStream(response.getBody()),
                repoName,
                "application/zip",
                FileEntity.FileType.SOLUTION,
                user.getId()
        );
    }


    public List<GithubRepositoryResponseDto> getAllUserReposNames(String userId) {
        String reposUri = "https://api.github.com/user/repos";

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        UserEntity user = userService.findUserById(userId);
        String accessToken = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<GithubRepositoryResponseDto[]> response = restClient.get()
                .uri(reposUri)
                .headers(header -> header.addAll(headers))
                .retrieve()
                .toEntity(GithubRepositoryResponseDto[].class);

        if (response.getBody() == null) {
            throw new ResourceNotFoundException("No repositories for user with id" + userId + "found");
        }

        return List.of(response.getBody());
    }
}
