package com.example.demo.service.file;

import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.model.file.FileEntity;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class GitHubFileService {

    private final FileUtilService fileUtilService;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public FileEntity downloadAndSaveRepoToZip(String repoName){
        String repoUrl = "https://api.github.com/repos/%s/%s/zipball/main";

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        String ownerId = userService.getCurrentUser().getUsername();
        String accessToken = client.getAccessToken().getTokenValue();
        String downloadUrl = String.format(repoUrl, ownerId, repoName);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = new RestTemplate().exchange(
                downloadUrl,
                HttpMethod.GET,
                entity,
                byte[].class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new ResourceNotFoundException("Resource not found");
        }

        return fileUtilService.uploadFile(
                new ByteArrayInputStream(response.getBody()),
                repoName,
                "application/zip",
                FileEntity.FileType.SOLUTION,
                ownerId
        );
    }
}
