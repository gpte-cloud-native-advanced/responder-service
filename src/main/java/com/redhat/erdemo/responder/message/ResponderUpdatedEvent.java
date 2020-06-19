package com.redhat.erdemo.responder.message;

import com.redhat.erdemo.responder.model.Responder;

public class ResponderUpdatedEvent {

    private String status;

    private String statusMessage;

    private Responder responder;

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Responder getResponder() {
        return responder;
    }

    public static class Builder {

        private final ResponderUpdatedEvent event;

        public Builder(String status, Responder responder) {
            event = new ResponderUpdatedEvent();
            event.responder = responder;
            event.status = status;
        }

        public Builder statusMessage(String statusMessage) {
            event.statusMessage = statusMessage;
            return this;
        }

        public ResponderUpdatedEvent build() {
            return event;
        }
    }

}
