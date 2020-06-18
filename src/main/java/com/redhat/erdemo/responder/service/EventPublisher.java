package com.redhat.erdemo.responder.service;

import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.erdemo.responder.message.Message;
import com.redhat.erdemo.responder.message.RespondersCreatedEvent;
import com.redhat.erdemo.responder.message.RespondersDeletedEvent;
import com.redhat.erdemo.responder.model.Responder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final UnicastProcessor<Pair<String, Message<?>>> processor = UnicastProcessor.create();

    public void responderCreated(Long id) {
        Message<RespondersCreatedEvent> message = new Message.Builder<>("RespondersCreatedEvent", "ResponderService",
                new RespondersCreatedEvent.Builder(new Long[]{id}).build()).build();
        processor.onNext(ImmutablePair.of(Integer.toString(id.hashCode()), message));
    }

    public void respondersCreated(List<Long> ids) {
        Message<RespondersCreatedEvent> message = new Message.Builder<>("RespondersCreatedEvent", "ResponderService",
                new RespondersCreatedEvent.Builder(ids.toArray(new Long[0])).build()).build();
        processor.onNext(ImmutablePair.of(Integer.toString(ids.hashCode()), message));
    }

    public void respondersDeleted(List<Long> ids) {
        Message<RespondersDeletedEvent> message = new Message.Builder<>("RespondersDeletedEvent", "ResponderService",
                new RespondersDeletedEvent.Builder(ids.toArray(new Long[0])).build()).build();
        processor.onNext(ImmutablePair.of(Integer.toString(ids.hashCode()), message));
    }

    public void responderUpdated(Triple<Boolean, String, Responder> status, Map<String, String> context) {
        throw new NotImplementedException("Not implemented");
    }

    @Outgoing("responder-event")
    public Multi<org.eclipse.microprofile.reactive.messaging.Message<String>> responderEvent() {
        return processor.onItem().apply(this::toMessage);
    }

    private org.eclipse.microprofile.reactive.messaging.Message<String> toMessage(Pair<String, Message<?>> pair) {

        String json = "";
        try {
            json = new ObjectMapper().writeValueAsString(pair.getRight());
        } catch (JsonProcessingException e) {
            log.error("Error serializing message to String", e);
        }

        return KafkaRecord.of(pair.getLeft(), json);
    }



}
