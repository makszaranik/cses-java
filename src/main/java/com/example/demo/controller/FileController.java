package com.example.demo.controller;

import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.model.file.FileEntity;
import com.example.demo.model.file.FileEntity.FileType;
import com.example.demo.service.file.FileUtilService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("files")
public class FileController {

    private final FileUtilService fileUtilService;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @PostMapping("upload")
    @PreAuthorize("isAuthenticated()")
    public FileEntity uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam FileType fileType) throws IOException {
        String ownerId = userService.getCurrentUser().getId();
        return fileUtilService.uploadFile(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                fileType,
                ownerId
        );
    }

    @GetMapping("download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") String fileId) {
        GridFsResource resourceFile = fileUtilService.getFileById(fileId);
        String contentType = resourceFile.getContentType();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(resourceFile.getFilename())
                        .build()
        );
        return ResponseEntity.ok().headers(headers).body(resourceFile);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("github-save-zip/{repoName}")
    public FileEntity downloadGithubRepo(@PathVariable String repoName) {
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
                FileType.SOLUTION,
                ownerId
        );
    }

}
