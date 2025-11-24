package com.example.demo.service.file;

import com.example.demo.model.file.FileEntity;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnusedFileRemover {

    private final FileUtilService fileUtilService;
    private final TaskService taskService;
    private final SubmissionService submissionService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void removeFiles() {
        List<FileEntity> allFiles = fileUtilService.findAllFiles();
        Set<String> usedFileIds = getAllFileUsages();

        List<FileEntity> unusedFiles = allFiles.stream()
                .filter(file -> !usedFileIds.contains(file.getId()))
                .toList();

        log.info("unused fileIds to remove: {}", unusedFiles.stream().map(FileEntity::getId).toList());

        unusedFiles.stream()
                .filter(file -> Duration.between(file.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 5)
                .forEach(fileUtilService::removeFile);
    }

    private Set<String> getAllFileUsages() {
        List<SubmissionEntity> submissions = submissionService.findAllSubmissions();
        List<TaskEntity> tasks = taskService.findAllTasks();
        Set<String> usedFileIds = new HashSet<>();

        //usages in tasks
        for (TaskEntity task : tasks) {
            if (task.getSolutionTemplateFileId() != null) {
                usedFileIds.add(task.getSolutionTemplateFileId());
            }
            if (task.getTestsFileId() != null) {
                usedFileIds.add(task.getTestsFileId());
            }
            if (task.getLintersFileId() != null) {
                usedFileIds.add(task.getLintersFileId());
            }
        }

        //usages in submissions
        for (SubmissionEntity submission : submissions) {
            if (submission.getSourceCodeFileId() != null) {
                usedFileIds.add(submission.getSourceCodeFileId());
            }
        }

        return usedFileIds;
    }

}
