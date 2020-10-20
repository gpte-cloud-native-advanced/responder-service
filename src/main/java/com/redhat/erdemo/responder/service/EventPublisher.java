package com.redhat.erdemo.responder.service;

import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.erdemo.responder.message.Message;
import com.redhat.erdemo.responder.message.ResponderUpdatedEvent;
import com.redhat.erdemo.responder.message.RespondersCreatedEvent;
import com.redhat.erdemo.responder.message.RespondersDeletedEvent;
import com.redhat.erdemo.responder.model.Responder;
import com.redhat.erdemo.responder.tracing.TracingKafkaUtils;
import io.opentracing.Tracer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.Json;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventPublisher {

    @Inject
    Tracer tracer;

    @ConfigProperty(name = "mp.messaging.outgoing.responder-event.topic")
    String responderEventTopic;

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
        Message.Builder<ResponderUpdatedEvent> builder = new Message.Builder<>("ResponderUpdatedEvent", "ResponderService",
                new ResponderUpdatedEvent.Builder(status.getLeft() ? "success" : "error", status.getRight())
                        .statusMessage(status.getMiddle()).build());
        context.forEach(builder::header);
        processor.onNext(ImmutablePair.of(status.getRight().getId(), builder.build()));
    }

    @Outgoing("responder-event")
    public Multi<org.eclipse.microprofile.reactive.messaging.Message<String>> responderEvent() {
        return processor.onItem().transform(this::toMessage);
    }

    private org.eclipse.microprofile.reactive.messaging.Message<String> toMessage(Pair<String, Message<?>> pair) {
        KafkaRecord<String, String> record = KafkaRecord.of(responderEventTopic, pair.getLeft(), Json.encode(pair.getRight()));
        TracingKafkaUtils.buildAndInjectSpan(record, tracer);
        return record;
    }

}
