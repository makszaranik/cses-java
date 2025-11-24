package com.example.demo.repository;

import com.example.demo.model.file.FileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<FileEntity, String> {

    List<FileEntity> findAllByOwnerId(String ownerId);

    Optional<FileEntity> findByGridFSFileId(String gridFSFileId);

}