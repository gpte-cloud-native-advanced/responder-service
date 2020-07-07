package com.redhat.erdemo.responder.repository;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ResponderRepositoryTest {

    @Inject
    ResponderRepository responderRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    UserTransaction transaction;

    @BeforeEach
    @Transactional
    void deleteAllFromTable() {
        entityManager.createQuery("DELETE FROM ResponderEntity").executeUpdate();
    }

    @Test
    void testInjectResponderRepository() {
        assertThat(responderRepository, notNullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      A ResponderEntity is created
     *
     *    Then:
     *      The id of the ResponderEntity is not equal to 0
     *
     */
    @Test
    @Transactional
    void testCreateEntity() {
        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Doe")
                .phoneNumber("111-222-333")
                .currentPositionLatitude(new BigDecimal("30.12345"))
                .currentPositionLongitude(new BigDecimal("-70.98765"))
                .boatCapacity(3)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        responderRepository.create(responder);
        assertThat(responder.getId(), not(equalTo(0)));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      A call is made to `availableResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      All the ResponderEntity objects have the field `available` set to true
     *      When called without limit or offset, the list contains all the ResponderEntity objects with field
     *          `available` set tot true
     *      When called with a limit, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset `n`, the first ResponderEntity object in the list corresponds to the
     *        n+1-th record
     *
     */
    @Test
    void testAvailableResponders() {

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder6 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5, responder6));

        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.availableResponders());
        assertThat(responders1.size(), equalTo(4));
        responders1.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        List<ResponderEntity> responders2 = template.execute(() -> responderRepository.availableResponders(2, 0));
        assertThat(responders2.size(), equalTo(2));
        responders2.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        List<ResponderEntity> responders3 = template.execute(() -> responderRepository.availableResponders(2, 2));
        assertThat(responders3.size(), equalTo(2));
        responders3.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        responders3.stream().map(ResponderEntity::getId).forEach(i -> assertThat(i, not(anyOf(equalTo(responders2.get(0).getId()), equalTo(responders2.get(1).getId())))));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      There are ResponderEntity records with field `enrolled` set to false in the database
     *      A call is made to `availableResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      All the ResponderEntity objects have the field `available` set to true
     *      All the ResponderEntity objects have the field enrolled` set to true
     *      When called without limit or offset, the list contains all the ResponderEntity objects with field
     *          `available` set tot true and `enrolled` set to true
     *      When called with a limit, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset `n`, the first ResponderEntity object in the list corresponds to the n+1 record
     *
     */
    @Test
    void testAvailableRespondersWhenNotEnrolled() {

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        ResponderEntity responder6 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(false)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5, responder6));

        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.availableResponders());
        assertThat(responders1.size(), equalTo(4));
        responders1.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        responders1.stream().map(ResponderEntity::isEnrolled).forEach(a -> assertThat(a, is(true)));
        List<ResponderEntity> responders2 = template.execute(() -> responderRepository.availableResponders(2, 0));
        assertThat(responders2.size(), equalTo(2));
        responders2.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        responders2.stream().map(ResponderEntity::isEnrolled).forEach(a -> assertThat(a, is(true)));
        List<ResponderEntity> responders3 = template.execute(() -> responderRepository.availableResponders(2, 2));
        assertThat(responders3.size(), equalTo(2));
        responders3.stream().map(ResponderEntity::isAvailable).forEach(a -> assertThat(a, is(true)));
        responders2.stream().map(ResponderEntity::isEnrolled).forEach(a -> assertThat(a, is(true)));
        responders3.stream().map(ResponderEntity::getId).forEach(i -> assertThat(i, not(anyOf(equalTo(responders2.get(0).getId()), equalTo(responders2.get(1).getId())))));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      There are ResponderEntity records with field `enrolled` set to false in the database
     *      There are ResponderEntity records with field `person` set to true in the database
     *      A call is made to `availableResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      When called without limit or offset, the list contains all the ResponderEntity objects with field
     *          `available` set tot true and `enrolled` set to true
     *      The ResponderEntity objects with field `person` equals true are the first records in the list
     */
    @Test
    void testAvailableRespondersOrderedByPerson() {

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder6 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5, responder6));

        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.availableResponders(6, 0));
        assertThat(responders1.size(), equalTo(4));
        assertThat(responders1.get(0).isPerson(), equalTo(true));
        assertThat(responders1.get(1).isPerson(), equalTo(true));
        assertThat(responders1.get(2).isPerson(), equalTo(false));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `allResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      When called without limit or offset, the list contains all the ResponderEntity objects in the database
     *      When called with a limit and offset, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset `n`, the first ResponderEntity object in the list corresponds to the n+1-th record
     *
     */
    @Test
    void testAllResponders() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Doe I")
                .phoneNumber("111-222-333")
                .currentPositionLatitude(new BigDecimal("30.12345"))
                .currentPositionLongitude(new BigDecimal("-70.98765"))
                .boatCapacity(3)
                .medicalKit(true)
                .available(false)
                .enrolled(false)
                .person(false)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Doe II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Doe III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(false)
                .person(false)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Doe IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Doe V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(false)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5));
        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.allResponders());
        assertThat(responders1.size(), equalTo(5));
        List<ResponderEntity> responders2 = template.execute(() -> responderRepository.allResponders(2,0));
        assertThat(responders2.size(), equalTo(2));
        List<ResponderEntity> responders3 = template.execute(() -> responderRepository.allResponders(2,2));
        responders3.stream().map(ResponderEntity::getId).forEach(i -> assertThat(i, not(anyOf(equalTo(responders2.get(0).getId()), equalTo(responders2.get(1).getId())))));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `person` set to true in the database
     *      A call is made to `personResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      All the ResponderEntity objects have the field `person` set to true
     *      When called without limit or offset, the list contains all the ResponderEntity objects with field
     *          `person` set to true
     *      When called with a limit, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset, the number of ResponderEntity objects corresponds to the limit
     *      When called with a limit and offset `n`, the first ResponderEntity object in the list corresponds to the n+1-th record
     *
     */
    @Test
    void testPersonResponders() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder6 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5, responder6));

        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.personResponders());
        assertThat(responders1.size(), equalTo(4));
        responders1.stream().map(ResponderEntity::isPerson).forEach(p -> assertThat(p, equalTo(true)));
        List<ResponderEntity> responders2 = template.execute(() -> responderRepository.personResponders(2,0));
        assertThat(responders2.size(), equalTo(2));
        responders2.stream().map(ResponderEntity::isPerson).forEach(p -> assertThat(p, equalTo(true)));
        List<ResponderEntity> responders3 = template.execute(() -> responderRepository.personResponders(2,2));
        responders3.stream().map(ResponderEntity::isPerson).forEach(p -> assertThat(p, equalTo(true)));
        responders3.stream().map(ResponderEntity::getId).forEach(i -> assertThat(i, not(anyOf(equalTo(responders2.get(0).getId()), equalTo(responders2.get(1).getId())))));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `person` set to false in the database
     *      A call is made to `nonPersonResponders`
     *
     *    Then:
     *      A list of ResponderEntity objects is returned
     *      All the ResponderEntity objects have the field `person` set to false
     *      The list contains all the ResponderEntity objects with field `person` set to false
     *
     */
    @Test
    void testNonPersonResponders() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder6 = new ResponderEntity.Builder()
                .name("John Foo V")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5, responder6));

        TransactionTemplate template = new TransactionTemplate(transaction);
        List<ResponderEntity> responders1 = template.execute(() -> responderRepository.nonPersonResponders());
        assertThat(responders1.size(), equalTo(2));
        responders1.stream().map(ResponderEntity::isPerson).forEach(p -> assertThat(p, equalTo(false)));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `findById` with parameter `id` corresponding to an existing ResponderEntity object
     *
     *    Then:
     *      A ResponderEntity object is returned
     *      The `id` field of the ResponderEntity is equal to the `id` parameter of the `findById` call
     *
     */
    @Test
    void testFindResponderById() {

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity result = template.execute(() -> responderRepository.findById(responder1.getId()));
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(responder1.getId()));
        assertThat(result.getName(), equalTo(responder1.getName()));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `findById` with parameter `id` not corresponding to an existing ResponderEntity object
     *
     *    Then:
     *      The call to `findId` returns null
     *
     */
    @Test
    void testFindByIdWhenResponderDoesNotExist() {

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity result = template.execute(() -> responderRepository.findById(responder1.getId()+10));
        assertThat(result, nullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity records in the database
     *      The ResponderEntity has the field `available` set to true.
     *      A call is made to `update` with as parameter a ResponderEntity object. The ResponderEntity parameter has
     *          the `available` field set to false
     *
     *    Then:
     *      The ResponderEntity record in the database is updated
     *      The ResponderEntity record in the database has the field `available` set to false.
     *      The return object of the `update` call is a Pair with boolean flag true and the updated ResponderEntity object
     *
     */
    @Test
    void testUpdateResponderEntityWhenStateIsChanged() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity toUpdate = new ResponderEntity.Builder(responder.getId()).available(false).build();
        Triple<Boolean, String, ResponderEntity> updated = template.execute(() -> responderRepository.update(toUpdate));
        assertThat(updated.getLeft(), equalTo(true));
        assertThat(updated.getRight(), notNullValue());
        assertThat(updated.getRight().isAvailable(), equalTo(false));
        ResponderEntity verify = template.execute(() -> responderRepository.findById(responder.getId()));
        assertThat(verify, notNullValue());
        assertThat(verify.isAvailable(), equalTo(false));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity records in the database
     *      The ResponderEntity has the field `available` set to false.
     *      A call is made to `update` with as parameter a ResponderEntity object. The ResponderEntity parameter has
     *          the `available` field set to true, and the `enrolled` field set to null
     *
     *    Then:
     *      The ResponderEntity record in the database is updated
     *      The ResponderEntity record in the database has the field `available` set to true.
     *      The ResponderEntity record in the database has the field `enrolled` set to false.
     *      The return object of the `update` call is a Pair with boolean flag true and the updated ResponderEntity object
     *
     */
    @Test
    void testUpdateResponderEntityWhenStateIsChangedAndPersonIsTrue() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity toUpdate = new ResponderEntity.Builder(responder.getId()).available(true).build();
        Triple<Boolean, String, ResponderEntity> updated = template.execute(() -> responderRepository.update(toUpdate));
        assertThat(updated.getLeft(), equalTo(true));
        assertThat(updated.getRight(), notNullValue());
        assertThat(updated.getRight().isAvailable(), equalTo(true));
        ResponderEntity verify = template.execute(() -> responderRepository.findById(responder.getId()));
        assertThat(verify, notNullValue());
        assertThat(verify.isAvailable(), equalTo(true));
        assertThat(verify.isEnrolled(), equalTo(false));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity records in the database
     *      The ResponderEntity has the field `available` set to true.
     *      A call is made to `update` with as parameter a ResponderEntity object. The ResponderEntity parameter has
     *          the `available` field set to true
     *
     *    Then:
     *      The ResponderEntity record in the database is not updated
     *      The ResponderEntity record in the database has the field `available` set to true.
     *      The return object of the `update` call is a Pair with boolean flag false and the ResponderEntity object
     *
     */
    @Test
    void testUpdateResponderEntityWhenStateIsNotChanged() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity toUpdate = new ResponderEntity.Builder(responder.getId()).available(true).build();
        Triple<Boolean, String, ResponderEntity> updated = template.execute(() -> responderRepository.update(toUpdate));
        assertThat(updated.getLeft(), equalTo(false));
        assertThat(updated.getRight(), notNullValue());
        assertThat(updated.getRight().isAvailable(), equalTo(true));
        ResponderEntity verify = template.execute(() -> responderRepository.findById(responder.getId()));
        assertThat(verify, notNullValue());
        assertThat(verify.isAvailable(), equalTo(true));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity records in the database
     *      A call is made to `update` with as parameter a ResponderEntity object. The ResponderEntity parameter has
     *          the `id` field set to a value not present in the database records
     *
     *    Then:
     *      The return object of the `update` call is a Pair with boolean flag false and no ResponderEntity object
     *
     */
    @Test
    void testUpdateResponderEntityWhenNotFound() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity toUpdate = new ResponderEntity.Builder(responder.getId()+10).available(false).build();
        Triple<Boolean, String, ResponderEntity> updated = template.execute(() -> responderRepository.update(toUpdate));
        assertThat(updated.getLeft(), equalTo(false));
        assertThat(updated.getRight(), nullValue());
        ResponderEntity verify = template.execute(() -> responderRepository.findById(responder.getId()));
        assertThat(verify, notNullValue());
        assertThat(verify.isAvailable(), equalTo(true));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity record in the database
     *      Two concurrent calls are made to `update` trying to update the ResponderEntity record
     *
     *    Then:
     *      Only one of the `update` calls succeeds.
     *      The state of the ResponderEntity in the database reflects the succeeded update.
     *
     */
    @Test
    void testUpdateResponderEntityWhenToUpdateIsStale() {

        List<Throwable> throwables = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch thread2Latch = new CountDownLatch(1);

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(false)
                .build();

        createResponders(Collections.singletonList(responder));

        Runnable thread1 = () -> {
            TransactionTemplate template = new TransactionTemplate(transaction);
            ResponderEntity updated = new ResponderEntity.Builder(responder.getId()).currentPositionLatitude(new BigDecimal("20.12345")).available(false).build();
            template.execute(() -> {
                try {
                    ResponderEntity toUpdate = responderRepository.findById(responder.getId());
                    thread2Latch.await(10, TimeUnit.SECONDS);

                    toUpdate.update(updated);
                    entityManager.flush();
                    assertThat("not expected", false);
                } catch (Exception e) {
                    assertThat(e, is(instanceOf(OptimisticLockException.class)));
                    throwables.add(e);
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
                return null;
            });
        };

        Runnable thread2 = () -> {
            TransactionTemplate template = new TransactionTemplate(transaction);
            ResponderEntity updated = new ResponderEntity.Builder(responder.getId()).currentPositionLatitude(new BigDecimal("30.12345")).available(false).build();
            template.execute(() -> {
                try {
                    ResponderEntity toUpdate = responderRepository.findById(responder.getId());

                    toUpdate.update(updated);
                    entityManager.flush();
                } finally {
                    thread2Latch.countDown();
                }
                return null;
            });
        };

        new Thread(thread1).start();
        new Thread(thread2).start();

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity verify = template.execute(() -> responderRepository.findById(responder.getId()));
        assertThat(verify.isAvailable(), equalTo(false));
        assertThat(verify.getCurrentPositionLatitude(), equalTo(new BigDecimal("30.12345")));

        assertThat(throwables.size(), equalTo(1));
        assertThat(throwables.get(0), instanceOf(OptimisticLockException.class));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity record in the database
     *      A call is made to `findByName` with parameter `name` corresponding to the `name` field of the ResponderEntity
     *           record.
     *
     *    Then:
     *      A ResponderEntity object is returned
     *      The ResponderEntity object corresponds to the record in the database
     *
     */
    @Test
    void testFindByNameWhenNameMatches() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity found = template.execute(() -> responderRepository.findByName("John Foo"));
        assertThat(found, notNullValue());
        assertThat(found.getName(), equalTo(responder.getName()));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There is a ResponderEntity record in the database
     *      A call is made to `findByName` with parameter `name` not corresponding to the `name` field of the ResponderEntity
     *           record.
     *
     *    Then:
     *      The `findByName` call returns null
     *
     */
    @Test
    void testFindByNameWhenNameDoesNotMatch() {

        ResponderEntity responder = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Collections.singletonList(responder));

        TransactionTemplate template = new TransactionTemplate(transaction);
        ResponderEntity found = template.execute(() -> responderRepository.findByName("John Doe"));
        assertThat(found, nullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are two ResponderEntity record in the database with exactly the same value for the `name` field
     *      A call is made to `findByName` with parameter `name` corresponding to the `name` field of the ResponderEntity
     *           records.
     *
     *    Then:
     *      The `findByName` call throws a `NonUniqueResultException` exception
     *
     */
    @Test
    void testFindByNameWhenMultipleNamesMatch() {

        List<Throwable> throwables = new ArrayList<>();

        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2));

        TransactionTemplate template = new TransactionTemplate(transaction);
        template.execute(() -> {
            try {
                responderRepository.findByName("John Foo");
            } catch (Exception e) {
                throwables.add(e);
            }
            return null;
        });
        assertThat(throwables.size(), equalTo(1));
        assertThat(throwables.get(0), instanceOf(NonUniqueResultException.class));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      There are ResponderEntity records with field `enrolled` set to false in the database
     *      There are ResponderEntity records with field `person` set to true in the database
     *      A call is made to `reset`
     *
     *    Then:
     *      All the ResponderEntity records have the value of `available` set to true.
     *      All the ResponderEntity records have the value of `enrolled` set to false.
     *      All the ResponderEntity records with `person` equals true have the `currentPositionLatitude` and
     *          `currentPositionLongitude` set to null
     *
     */
    @Test
    void testReset() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(true)
                .available(false)
                .enrolled(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3));

        TransactionTemplate template = new TransactionTemplate(transaction);
        template.execute(() -> {
            responderRepository.reset();
            return null;
        });
        ResponderEntity result1 = template.execute(() -> responderRepository.findById(responder1.getId()));
        assertThat(result1, notNullValue());
        assertThat(result1.isEnrolled(), equalTo(false));
        assertThat(result1.isAvailable(), equalTo(true));
        assertThat(result1.isPerson(), equalTo(false));
        assertThat(result1.getCurrentPositionLatitude(), notNullValue());
        assertThat(result1.getCurrentPositionLongitude(), notNullValue());

        ResponderEntity result2 = template.execute(() -> responderRepository.findById(responder2.getId()));
        assertThat(result2, notNullValue());
        assertThat(result2.isEnrolled(), equalTo(false));
        assertThat(result2.isAvailable(), equalTo(true));
        assertThat(result2.isPerson(), equalTo(false));
        assertThat(result2.getCurrentPositionLatitude(), notNullValue());
        assertThat(result2.getCurrentPositionLongitude(), notNullValue());

        ResponderEntity result3 = template.execute(() -> responderRepository.findById(responder3.getId()));
        assertThat(result3, notNullValue());
        assertThat(result3.isEnrolled(), equalTo(false));
        assertThat(result3.isAvailable(), equalTo(true));
        assertThat(result3.isPerson(), equalTo(true));
        assertThat(result3.getCurrentPositionLatitude(), nullValue());
        assertThat(result3.getCurrentPositionLongitude(), nullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      There are ResponderEntity records with field `enrolled` set to false in the database
     *      There are ResponderEntity records with field `person` set to true in the database
     *      A call is made to `clear`
     *
     *    Then:
     *      All the ResponderEntity records with `person` equals false have `available` set to false and `enrolled`
     *          set to false
     *      All the ResponderEntity records with `person` equals true have the `currentPositionLatitude` and
     *          `currentPositionLongitude` set to null, `available` set to true, `enrolled` set to false.
     *
     */
    @Test
    void testClear() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(true)
                .available(false)
                .enrolled(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3));

        TransactionTemplate template = new TransactionTemplate(transaction);
        template.execute(() -> {
            responderRepository.clear();
            return null;
        });

        ResponderEntity result1 = template.execute(() -> responderRepository.findById(responder1.getId()));
        assertThat(result1, notNullValue());
        assertThat(result1.isAvailable(), is(false));
        assertThat(result1.isEnrolled(), is(false));

        ResponderEntity result2 = template.execute(() -> responderRepository.findById(responder2.getId()));
        assertThat(result2, notNullValue());
        assertThat(result2.isAvailable(), is(false));
        assertThat(result2.isEnrolled(), is(false));

        ResponderEntity result3 = template.execute(() -> responderRepository.findById(responder3.getId()));
        assertThat(result3, notNullValue());
        assertThat(result3.isEnrolled(), equalTo(false));
        assertThat(result3.isAvailable(), equalTo(true));
        assertThat(result3.isPerson(), equalTo(true));
        assertThat(result3.getCurrentPositionLatitude(), nullValue());
        assertThat(result3.getCurrentPositionLongitude(), nullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      There are ResponderEntity records with field `available` set to true in the database
     *      There are ResponderEntity records with field `enrolled` set to false in the database
     *      There are ResponderEntity records with field `person` set to true in the database
     *      A call is made to `resetPersonsDeleteBots`
     *
     *    Then:
     *      All the ResponderEntity records with `person` equals false are deleted from the database
     *      All the ResponderEntity records with `person` equals true have the `currentPositionLatitude` and
     *          `currentPositionLongitude` set to null, `available` set to true, `enrolled` set to false.
     *
     */
    @Test
    void testResetPersonsDeleteBots() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(true)
                .available(false)
                .enrolled(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3));

        TransactionTemplate template = new TransactionTemplate(transaction);
        template.execute(() -> {
            responderRepository.resetPersonsDeleteBots();
            return null;
        });

        ResponderEntity result1 = template.execute(() -> responderRepository.findById(responder1.getId()));
        assertThat(result1, nullValue());

        ResponderEntity result2 = template.execute(() -> responderRepository.findById(responder2.getId()));
        assertThat(result2, nullValue());

        ResponderEntity result3 = template.execute(() -> responderRepository.findById(responder3.getId()));
        assertThat(result3, notNullValue());
        assertThat(result3.isEnrolled(), equalTo(false));
        assertThat(result3.isAvailable(), equalTo(true));
        assertThat(result3.isPerson(), equalTo(true));
        assertThat(result3.getCurrentPositionLatitude(), nullValue());
        assertThat(result3.getCurrentPositionLongitude(), nullValue());
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `deleteAll`
     *
     *    Then:
     *      All the ResponderEntity records are deleted from the database
     *
     */
    @Test
    void testDeleteAll() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(false)
                .available(false)
                .enrolled(true)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .person(true)
                .available(false)
                .enrolled(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3));

        TransactionTemplate template = new TransactionTemplate(transaction);
        template.execute(() -> {
            responderRepository.deleteAll();
            return null;
        });

        List<ResponderEntity> entities = template.execute(() -> responderRepository.allResponders());
        assertThat(entities.size(), is(equalTo(0)));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `activeRespondersCount`
     *
     *    Then:
     *      The call returns the count of the ResponderEntity records with `enrolled` equals = true and
     *          `available` equals false
     *
     */
    @Test
    void testActiveRespondersCount() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(false)
                .person(true)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(false)
                .person(false)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5));

        TransactionTemplate template = new TransactionTemplate(transaction);
        Long count = template.execute(() -> responderRepository.activeRespondersCount());
        assertThat(count, equalTo(2L));
    }

    /**
     *  Test description:
     *
     *    When:
     *      There are ResponderEntity records in the database
     *      A call is made to `enrolledRespondersCount`
     *
     *    Then:
     *      The call returns the count of the ResponderEntity records with `enrolled` equals = true
     *
     */
    @Test
    void testEnrolledRespondersCount() {
        ResponderEntity responder1 = new ResponderEntity.Builder()
                .name("John Foo I")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        ResponderEntity responder2 = new ResponderEntity.Builder()
                .name("John Foo II")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(true)
                .person(false)
                .build();

        ResponderEntity responder3 = new ResponderEntity.Builder()
                .name("John Foo III")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(false)
                .person(true)
                .build();

        ResponderEntity responder4 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(false)
                .enrolled(false)
                .person(false)
                .build();

        ResponderEntity responder5 = new ResponderEntity.Builder()
                .name("John Foo IV")
                .phoneNumber("999-888-777")
                .currentPositionLatitude(new BigDecimal("35.12345"))
                .currentPositionLongitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .enrolled(true)
                .person(true)
                .build();

        createResponders(Arrays.asList(responder1, responder2, responder3, responder4, responder5));

        TransactionTemplate template = new TransactionTemplate(transaction);
        Long count = template.execute(() -> responderRepository.enrolledRespondersCount());
        assertThat(count, equalTo(3L));
    }

    @Transactional
    void createResponders(List<ResponderEntity> responders) {
        responders.forEach(r -> entityManager.persist(r));
    }

    public static class TransactionTemplate {

        private final UserTransaction transaction;

        public TransactionTemplate(UserTransaction transaction) {
            this.transaction = transaction;
        }

        public <T> T execute(TransactionCallback<T> action) {
            try {
                transaction.begin();
                T result = action.doInTransaction();
                transaction.commit();
                return result;
            } catch (Exception e) {
                try {
                    transaction.rollback();
                } catch (Exception systemException) {
                    //ignore;
                }
                return null;
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {

        T doInTransaction();

    }


}
