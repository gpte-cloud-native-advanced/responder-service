package com.redhat.erdemo.responder.repository;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ResponderRepository {

    private static final Logger log = LoggerFactory.getLogger(ResponderRepository.class);

    @Inject
    EntityManager entityManager;

    public ResponderEntity create(ResponderEntity responder) {
        entityManager.persist(responder);
        return responder;
    }

    public ResponderEntity findById(long id) {
        return entityManager.find(ResponderEntity.class, id, LockModeType.OPTIMISTIC);
    }

    public ResponderEntity findByName(String name) {
        List<ResponderEntity> results = entityManager.createNamedQuery("Responder.findByName", ResponderEntity.class)
                .setParameter("name", name).getResultList();
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new NonUniqueResultException("Found several Responders with name '" + name + "'");
        }
    }

    public Triple<Boolean, String, ResponderEntity> update(ResponderEntity responder) {

        ResponderEntity toUpdate = findById(responder.getId());
        if (toUpdate == null) {
            log.warn("Responder with id '" + responder.getId() + "' not found in the database");
            return new ImmutableTriple<>(false, "Responder with id + " + responder.getId() + " not found.", null);
        }

        if (!stateChanged(toUpdate, responder)) {
            return new ImmutableTriple<>(false, "Responder state not changed", responder);
        }
        try {
            toUpdate.update(responder);
            entityManager.flush();
            return new ImmutableTriple<>(true, "Responder updated", toUpdate);
        } catch (Exception e) {
            log.warn("Exception '" + e.getClass() + "' when updating Responder with id '" + responder.getId() + "'. Responder record is not updated.");
            return new ImmutableTriple<>(false, "Exception '" + e.getClass() + "' when updating Responder", responder);
        }

    }

    public List<ResponderEntity> availableResponders() {
        return entityManager.createNamedQuery("Responder.availableResponders", ResponderEntity.class)
                .getResultList();
    }

    public List<ResponderEntity> availableResponders(int limit, int offset) {
        TypedQuery<ResponderEntity> q = entityManager.createNamedQuery("Responder.availableRespondersOrderedByPerson", ResponderEntity.class);
        if (limit > 0 && offset >= 0) {
            q.setMaxResults(limit);
            q.setFirstResult(offset);
        }
        return q.getResultList();
    }

    public List<ResponderEntity> allResponders() {
        return entityManager.createNamedQuery("Responder.allResponders", ResponderEntity.class).getResultList();
    }

    public List<ResponderEntity> allResponders(int limit, int offset) {
        TypedQuery<ResponderEntity> q = entityManager.createNamedQuery("Responder.allResponders", ResponderEntity.class);
        if (limit > 0 && offset >= 0) {
            q.setMaxResults(limit);
            q.setFirstResult(offset);
        }
        return q.getResultList();
    }

    public List<ResponderEntity> personResponders() {
        return entityManager.createNamedQuery("Responder.persons", ResponderEntity.class).getResultList();
    }

    public List<ResponderEntity> personResponders(int limit, int offset) {
        TypedQuery<ResponderEntity> q = entityManager.createNamedQuery("Responder.persons", ResponderEntity.class);
        if (limit > 0 && offset >= 0) {
            q.setMaxResults(limit);
            q.setFirstResult(offset);
        }
        return q.getResultList();
    }

    public List<ResponderEntity> nonPersonResponders() {
        return entityManager.createNamedQuery("Responder.nonPersons", ResponderEntity.class).getResultList();
    }

    public void reset() {
        entityManager.createNamedQuery("Responder.reset").executeUpdate();
        entityManager.createNamedQuery("Responder.resetPerson").executeUpdate();
        entityManager.flush();
    }

    public void clear() {
        entityManager.createNamedQuery("Responder.clearNonPersons").executeUpdate();
        entityManager.createNamedQuery("Responder.resetPerson").executeUpdate();
        entityManager.flush();
    }

    public void deleteAll() {
        entityManager.createNamedQuery("Responder.deleteAll").executeUpdate();
        entityManager.flush();
    }

    public Long enrolledRespondersCount() {
        return (Long) entityManager.createNamedQuery("Responder.countEnrolled").getSingleResult();
    }

    public Long activeRespondersCount() {
        return (Long) entityManager.createNamedQuery("Responder.countActive").getSingleResult();
    }

    private boolean stateChanged(ResponderEntity toUpdate, ResponderEntity updated) {

        if (updated.getName() != null && !updated.getName().equals(toUpdate.getName())) {
            return true;
        }
        if (updated.getPhoneNumber() != null && !updated.getPhoneNumber().equals(toUpdate.getPhoneNumber())) {
            return true;
        }
        if (updated.getCurrentPositionLatitude() != null && !updated.getCurrentPositionLatitude().equals(toUpdate.getCurrentPositionLatitude())) {
            return true;
        }
        if (updated.getCurrentPositionLongitude() != null && !updated.getCurrentPositionLongitude().equals(toUpdate.getCurrentPositionLongitude())) {
            return true;
        }
        if (updated.getBoatCapacity() != null && !updated.getBoatCapacity().equals(toUpdate.getBoatCapacity())) {
            return true;
        }
        if (updated.getMedicalKit() != null && !updated.getMedicalKit().equals(toUpdate.getMedicalKit())) {
            return true;
        }
        if (updated.isAvailable() != null && !updated.isAvailable().equals(toUpdate.isAvailable())) {
            return true;
        }
        if (updated.isPerson() != null && !updated.isPerson().equals(toUpdate.isPerson())) {
            return true;
        }
        if (updated.isEnrolled() != null && !updated.isEnrolled().equals(toUpdate.isEnrolled())) {
            return true;
        }
        return false;
    }

}
