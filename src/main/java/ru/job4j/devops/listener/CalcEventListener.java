package ru.job4j.devops.listener;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.job4j.devops.models.CalcEvent;
import ru.job4j.devops.service.CalcEventService;

@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "CalcEventService is a Spring-managed bean and is effectively immutable"
)
@Component
@RequiredArgsConstructor
@Slf4j
public class CalcEventListener {

    private final CalcEventService eventService;

    @KafkaListener(topics = "event", groupId = "job4j")
    public void signup(CalcEvent calcEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.debug("Received Event from topic {}: with User name: {}", topic, calcEvent.getUser().getName());

            eventService.logSomeCalcEvent(calcEvent);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            throw e;
        }
    }
}
