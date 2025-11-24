package com.example.demo.service.task;

import com.example.demo.model.task.TaskEntity;
import com.example.demo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public String save(TaskEntity task) {
        return taskRepository.save(task).getId();
    }

    public List<TaskEntity> findAllTasks(){
        return taskRepository.findAll();
    }

    public TaskEntity findTaskById(String id){
        return taskRepository.findTaskEntityById(id).orElseThrow(() ->
                new IllegalStateException(String.format("Task with id %s not found", id))
        );
    }

    public List<TaskEntity> findAllByOwnerId(String ownerId) {
        return taskRepository.findAllByOwnerId(ownerId);
    }


    public void removeTaskEntity(String id){
        TaskEntity taskToDelete = findTaskById(id);
        taskRepository.delete(taskToDelete);
    }

    public TaskEntity updateTask(TaskEntity task){
        return taskRepository.save(task);
    }
}
