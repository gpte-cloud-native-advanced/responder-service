package com.redhat.erdemo.responder.service;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodePresent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventPublisherTest {

    @Inject
    EventPublisher eventPublisher;

    @Inject @Any
    InMemoryConnector connector;

    @BeforeEach
    void init() {
        connector.sink("responder-event").clear();
    }

    @Test
    void testResponderCreated() {

        InMemorySink<String> results = connector.sink("responder-event");

        eventPublisher.responderCreated(1L);
        assertThat(results.received().size(), equalTo(1));
        Message<String> message = results.received().get(0);
        assertThat(message, instanceOf(OutgoingKafkaRecord.class));
        String value = message.getPayload();
        String key = ((OutgoingKafkaRecord<String, String>)message).getKey();
        assertThat(key, equalTo("1"));
        assertThat(value, jsonNodePresent("id"));
        assertThat(value, jsonPartEquals("messageType", "RespondersCreatedEvent"));
        assertThat(value, jsonPartEquals("invokingService", "ResponderService"));
        assertThat(value, jsonNodePresent("timestamp"));
        assertThat(value, jsonNodePresent("body"));
        assertThat(value, jsonPartEquals("body.created", 1));
        assertThat(value, jsonPartEquals("body.responders[0]", 1));
    }

    @Test
    void testRespondersCreated() {

        InMemorySink<String> results = connector.sink("responder-event");

        eventPublisher.respondersCreated(Arrays.asList(1L, 2L, 3L));
        assertThat(results.received().size(), equalTo(1));
        Message<String> message = results.received().get(0);
        assertThat(message, instanceOf(OutgoingKafkaRecord.class));
        String value = message.getPayload();
        String key = ((OutgoingKafkaRecord<String, String>)message).getKey();
        assertThat(key, equalTo("30817"));
        assertThat(value, jsonNodePresent("id"));
        assertThat(value, jsonPartEquals("messageType", "RespondersCreatedEvent"));
        assertThat(value, jsonPartEquals("invokingService", "ResponderService"));
        assertThat(value, jsonNodePresent("timestamp"));
        assertThat(value, jsonNodePresent("body"));
        assertThat(value, jsonPartEquals("body.created", 3));
        assertThat(value, jsonPartEquals("body.responders[0]", 1));
        assertThat(value, jsonPartEquals("body.responders[1]", 2));
        assertThat(value, jsonPartEquals("body.responders[2]", 3));
    }

    @Test
    void testRespondersDeleted() {

        InMemorySink<String> results = connector.sink("responder-event");

        eventPublisher.respondersDeleted(Arrays.asList(1L, 2L, 3L));
        assertThat(results.received().size(), equalTo(1));
        Message<String> message = results.received().get(0);
        assertThat(message, instanceOf(OutgoingKafkaRecord.class));
        String value = message.getPayload();
        String key = ((OutgoingKafkaRecord<String, String>)message).getKey();
        assertThat(key, equalTo("30817"));
        assertThat(value, jsonNodePresent("id"));
        assertThat(value, jsonPartEquals("messageType", "RespondersDeletedEvent"));
        assertThat(value, jsonPartEquals("invokingService", "ResponderService"));
        assertThat(value, jsonNodePresent("timestamp"));
        assertThat(value, jsonNodePresent("body"));
        assertThat(value, jsonPartEquals("body.deleted", 3));
        assertThat(value, jsonPartEquals("body.responders[0]", 1));
        assertThat(value, jsonPartEquals("body.responders[1]", 2));
        assertThat(value, jsonPartEquals("body.responders[2]", 3));
    }

}
