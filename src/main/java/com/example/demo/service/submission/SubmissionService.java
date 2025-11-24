package com.example.demo.service.submission;


import com.example.demo.dto.task.stats.TaskStatsResponseDto;
import com.example.demo.dto.task.stats.UserStatsResponseDto;
import com.example.demo.dto.task.TaskSubmissionRequestDto;
import com.example.demo.exceptions.SubmissionNotFoundException;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.repository.SubmissionRepository;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    public SubmissionEntity createSubmission(TaskSubmissionRequestDto submitDto) {
        SubmissionEntity taskSubmission = SubmissionEntity.builder()
                .taskId(submitDto.taskId())
                .userId(userService.getCurrentUser().getId())
                .sourceCodeFileId(submitDto.sourceCodeFileId())
                .status(SubmissionEntity.Status.SUBMITTED)
                .logs("")
                .score(0)
                .build();

        return submissionRepository.save(taskSubmission);
    }

    public Integer getNumberSubmissionsForTask(String userId, String taskId) {
        return submissionRepository.countByUserIdAndTaskId(userId, taskId);
    }

    public List<SubmissionEntity> getAllSubmitted() {
        return submissionRepository.findAllByStatus(SubmissionEntity.Status.SUBMITTED);
    }

    public List<SubmissionEntity> findAllSubmissions() {
        return submissionRepository.findAll();
    }

    public SubmissionEntity findSubmissionById(String id) {
        return submissionRepository.findSubmissionEntityById(id)
                .orElseThrow(() -> new SubmissionNotFoundException(id));
    }

    public void save(SubmissionEntity submissionEntity) {
        submissionRepository.save(submissionEntity);
    }

    public TaskStatsResponseDto getStatisticsForTask(String userId, String taskId) {
        List<SubmissionRepository.StatusWrapper> statuses =
                submissionRepository.getTaskStatusStatistics(userId, taskId);

        return new TaskStatsResponseDto(taskId, statuses);
    }

    public List<SubmissionEntity> getSubmissionsByUser(String userId) {
        return submissionRepository.findAllByUserId(userId);
    }


}
