package com.example.demo.repository;

import com.example.demo.model.submission.SubmissionEntity;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubmissionRepository extends MongoRepository<SubmissionEntity, String> {

    List<SubmissionEntity> findAllByStatus(SubmissionEntity.Status status);

    Optional<SubmissionEntity> findSubmissionEntityById(String id);

    List<SubmissionEntity> findAllByUserId(String userId);

    Integer countByUserIdAndTaskId(String userId, String taskId);

    @Aggregation(pipeline = {
            "{ $match: { userId: ?0, taskId: ?1} }",
            "{ $group: { _id: '$status', count: { $sum: 1 } } }",
            "{ $project: { _id: 0, status: '$_id', count: 1 } }"
    })
    List<StatusWrapper> getTaskStatusStatistics(String userId, String taskId);

    record StatusWrapper(SubmissionEntity.Status status, Integer count) {}

}