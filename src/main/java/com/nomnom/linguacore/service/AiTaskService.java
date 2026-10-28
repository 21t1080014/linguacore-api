package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.response.AiTaskResponse;
import com.nomnom.linguacore.entity.AiTask;
import com.nomnom.linguacore.entity.Card;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.repository.AiTaskRepository;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class AiTaskService {
    private static final String AI_TASK_QUEUE = "ai.task.queue";
    private final RabbitTemplate template;
    private final AiTaskRepository repository;
    private UserRepository userRepository;

    public AiTaskService(RabbitTemplate template, AiTaskRepository repository, UserRepository userRepository) {
        this.template = template;
        this.repository = repository;
        this.userRepository = userRepository;
    }
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Không xác định được người dùng"));
    }

    public AiTaskResponse createExampleGenerationTask(Card card){

        AiTask task = new AiTask();
        task.setTaskType("EXAMPLE_GENERATION");
        task.setStatus("PENDING");
        task.setCard(card);
        task.setUser(getCurrentUser());
        task.setRetryCount(0);

        AiTask savedTask = repository.save(task);

        template.convertAndSend(
                AI_TASK_QUEUE,
                savedTask.getId().toString()
        );

        return new AiTaskResponse(
                savedTask.getId(),
                savedTask.getTaskType(),
                savedTask.getStatus(),
                savedTask.getCard() != null ? savedTask.getCard().getId() : null,
                savedTask.getResult(),
                savedTask.getCreatedAt(),
                savedTask.getUpdatedAt()
        );
    }
    private AiTask findOwnedAiTask(Long id) {
        AiTask ai = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AiTask not found: " + id));
        if (!ai.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ResourceNotFoundException("AiTask not found: " + id);
        }
        return ai;
    }
    public AiTaskResponse getTaskStatus(Long id){
        AiTask ai = findOwnedAiTask(id);
        return new AiTaskResponse(
                ai.getId(),
                ai.getTaskType(),
                ai.getStatus(),
                ai.getCard() != null ? ai.getCard().getId() : null,
                ai.getResult(),
                ai.getCreatedAt(),
                ai.getUpdatedAt()
        );
    }
}
