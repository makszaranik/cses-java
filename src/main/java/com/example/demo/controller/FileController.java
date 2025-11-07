package com.example.demo.controller;


import com.example.demo.model.file.FileEntity;
import com.example.demo.model.file.FileEntity.FileType;
import com.example.demo.model.user.UserEntity.UserRole;
import com.example.demo.security.PreAuthorize;
import com.example.demo.security.UserContext;
import com.example.demo.service.file.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("files")
public class FileController {

    private final FileUtilService fileUtilService;
    private final UserContext context;

    @PostMapping("upload")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(roles = {UserRole.STUDENT, UserRole.ADMIN, UserRole.TEACHER})
    public FileEntity uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam FileType fileType) {
        String ownerId = context.get().getId();
        return fileUtilService.uploadFile(file, fileType, ownerId);
    }


    @GetMapping("download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") String fileId) {
        GridFsResource resourceFile = fileUtilService.getFileById(fileId);
        String contentType = resourceFile.getContentType();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(resourceFile.getFilename())
                .build());
        return ResponseEntity.ok().headers(headers).body(resourceFile);
    }
}
