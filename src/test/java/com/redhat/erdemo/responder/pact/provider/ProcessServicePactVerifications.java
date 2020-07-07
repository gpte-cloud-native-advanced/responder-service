package com.redhat.erdemo.responder.pact.provider;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("responder-service")
@PactFolder("pact")
public class ProcessServicePactVerifications {

    @BeforeEach
    void before(PactVerificationContext context) throws MalformedURLException {
        context.setTarget(HttpTestTarget.fromUrl(providerUrl()));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("Available responders")
    void stateAvailableResponders() {
        String url = url();
        String user = user();
        String password = password();

        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database URL, user or password cannot be null.");
        }

        String clear = "DELETE FROM public.responder";
        String responder1 = "INSERT INTO public.responder( " +
                "responder_id, responder_name, responder_phone_number, responder_current_gps_lat, responder_current_gps_long, boat_capacity, has_medical_kit, available, person, enrolled, version) " +
                "VALUES (nextval('responder_sequence'), 'John Doe', '(456) 123-9874', 30.12345, -70.98765, 5, true, true, true, true, 0)";
        String responder2 = "INSERT INTO public.responder( " +
                "responder_id, responder_name, responder_phone_number, responder_current_gps_lat, responder_current_gps_long, boat_capacity, has_medical_kit, available, person, enrolled, version) " +
                "VALUES (nextval('responder_sequence'), 'Jane Foo', '(654) 741-9632', 30.23456, -70.87654, 10, true, true, true, true, 0)";
        String responder3 = "INSERT INTO public.responder( " +
                "responder_id, responder_name, responder_phone_number, responder_current_gps_lat, responder_current_gps_long, boat_capacity, has_medical_kit, available, person, enrolled, version) " +
                "VALUES (nextval('responder_sequence'), 'Pete Who', '(123) 159-9654', 30.34567, -70.76543, 5, true, false, true, true, 0)";

        try (Connection connection = connection(url, user, password);
             Statement statement = connection.createStatement()) {

            statement.addBatch(clear);
            statement.addBatch(responder1);
            statement.addBatch(responder2);
            statement.addBatch(responder3);
            statement.executeBatch();
            connection.commit();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connection(String url, String user, String password) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        return connection;
    }

    private String url() {
        return System.getenv("PACT_STAGE_DATABASE_URL");
    }

    private String user() {
        return System.getenv("PACT_STAGE_DATABASE_USER");
    }

    private String password() {
        return System.getenv("PACT_STAGE_DATABASE_PASSWD");
    }

    private URL providerUrl() throws MalformedURLException {
        String provider = System.getenv("PACT_VERIFICATION_TARGET");
        if (provider == null) {
            throw new MalformedURLException("Provider URL is null");
        }
        return new URL(provider);
    }
}
