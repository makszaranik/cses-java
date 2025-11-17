package com.example.demo.controller;

import com.example.demo.model.file.FileEntity;
import com.example.demo.model.file.FileEntity.FileType;
import com.example.demo.service.file.FileUtilService;
import com.example.demo.service.file.GitHubFileService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("files")
public class FileController {

    private final FileUtilService fileUtilService;
    private final UserService userService;
    private final GitHubFileService gitHubFileService;

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
        return gitHubFileService.downloadAndSaveRepoToZip(repoName);
    }
}
