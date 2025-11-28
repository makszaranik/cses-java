package com.example.demo.service.event;

import com.example.demo.model.submission.SubmissionEntity;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListener;

import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final List<SubmissionStatusEvent> emitters = new Vector<>();
    private final MessageListenerContainer listenerContainer;

    @PostConstruct
    public void startListening() {
        MessageListener<ChangeStreamDocument<Document>, SubmissionEntity> submissionEntityMessageListener = m -> {
            log.debug("Received change stream document {}", m);
            ChangeStreamDocument<Document> raw = m.getRaw();
            if (raw != null && raw.getDocumentKey() != null) {
                OperationType operationType = raw.getOperationType();
                String documentKey = raw.getDocumentKey().getFirstKey();
                log.debug("Message with type {} for: submission {} with id {}", operationType, raw, documentKey);

                switch (operationType) {
                    case UPDATE, REPLACE -> {
                        if (m.getBody() != null) {
                            sendSubmissionStatusEvent(m.getBody());
                        }
                    }
                    case null -> log.error("Operation type {} is not supported for: submission {} with id {}", operationType, raw, documentKey);
                    default -> log.error("Unsupported operation type {} for submission {}", operationType, raw.getDocumentKey());
                }
            }
        };

        ChangeStreamRequest<SubmissionEntity> request = ChangeStreamRequest.builder(submissionEntityMessageListener)
                .collection("submissions")
                .build();

        listenerContainer.start();
        listenerContainer.register(request, SubmissionEntity.class);

    }

    record SubmissionStatusEvent(
            String userId,
            String submissionId,
            SseEmitter emitter
    ) {}

    public void createSubmissionStatusEvent(SseEmitter emitter, String userId, String submissionId) {
        log.debug("Creating submission status event for submission id {}", submissionId);
        SubmissionStatusEvent statusEvent = new SubmissionStatusEvent(userId, submissionId, emitter);
        emitters.add(statusEvent);
        emitter.onCompletion(() -> emitters.remove(statusEvent));
        emitter.onTimeout(() -> emitters.remove(statusEvent));
        emitter.onError((error) -> emitters.remove(statusEvent));
    }

    private void sendSubmissionStatusEvent(SubmissionEntity submissionEntity) {
        emitters.forEach(event -> {
            SseEmitter emitter = event.emitter;
            String submissionId = event.submissionId;
            if(submissionId.equals(submissionEntity.getId())) {
                try {
                    log.debug("Sending submission status event for submission id {}", submissionId);
                    emitter.send(submissionEntity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
