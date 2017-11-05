/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.gestatech.hr.ejb.domain.controler;

import be.gestatech.hr.ejb.domain.controler.exceptions.IllegalOrphanException;
import be.gestatech.hr.ejb.domain.controler.exceptions.NonexistentEntityException;
import be.gestatech.hr.ejb.domain.controler.exceptions.PreexistingEntityException;
import be.gestatech.hr.ejb.domain.controler.exceptions.RollbackFailureException;
import be.gestatech.hr.ejb.domain.entity.Countries;
import be.gestatech.hr.ejb.domain.entity.Locations;
import be.gestatech.hr.ejb.domain.entity.Regions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author amuri
 */
public class CountriesJpaController implements Serializable {

    public CountriesJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Countries countries) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (countries.getLocationsCollection() == null) {
            countries.setLocationsCollection(new ArrayList<>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Regions regionId = countries.getRegionId();
            if (regionId != null) {
                regionId = em.getReference(regionId.getClass(), regionId.getRegionId());
                countries.setRegionId(regionId);
            }
            Collection<Locations> attachedLocationsCollection = new ArrayList<Locations>();
            for (Locations locationsCollectionLocationsToAttach : countries.getLocationsCollection()) {
                locationsCollectionLocationsToAttach = em.getReference(locationsCollectionLocationsToAttach.getClass(), locationsCollectionLocationsToAttach.getLocationId());
                attachedLocationsCollection.add(locationsCollectionLocationsToAttach);
            }
            countries.setLocationsCollection(attachedLocationsCollection);
            em.persist(countries);
            if (regionId != null) {
                regionId.getCountriesCollection().add(countries);
                regionId = em.merge(regionId);
            }
            for (Locations locationsCollectionLocations : countries.getLocationsCollection()) {
                Countries oldCountryIdOfLocationsCollectionLocations = locationsCollectionLocations.getCountryId();
                locationsCollectionLocations.setCountryId(countries);
                locationsCollectionLocations = em.merge(locationsCollectionLocations);
                if (oldCountryIdOfLocationsCollectionLocations != null) {
                    oldCountryIdOfLocationsCollectionLocations.getLocationsCollection().remove(locationsCollectionLocations);
                    oldCountryIdOfLocationsCollectionLocations = em.merge(oldCountryIdOfLocationsCollectionLocations);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCountries(countries.getCountryId()) != null) {
                throw new PreexistingEntityException("Countries " + countries + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Countries countries) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Countries persistentCountries = em.find(Countries.class, countries.getCountryId());
            Regions regionIdOld = persistentCountries.getRegionId();
            Regions regionIdNew = countries.getRegionId();
            Collection<Locations> locationsCollectionOld = persistentCountries.getLocationsCollection();
            Collection<Locations> locationsCollectionNew = countries.getLocationsCollection();
            List<String> illegalOrphanMessages = null;
            for (Locations locationsCollectionOldLocations : locationsCollectionOld) {
                if (!locationsCollectionNew.contains(locationsCollectionOldLocations)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Locations " + locationsCollectionOldLocations + " since its countryId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (regionIdNew != null) {
                regionIdNew = em.getReference(regionIdNew.getClass(), regionIdNew.getRegionId());
                countries.setRegionId(regionIdNew);
            }
            Collection<Locations> attachedLocationsCollectionNew = new ArrayList<Locations>();
            for (Locations locationsCollectionNewLocationsToAttach : locationsCollectionNew) {
                locationsCollectionNewLocationsToAttach = em.getReference(locationsCollectionNewLocationsToAttach.getClass(), locationsCollectionNewLocationsToAttach.getLocationId());
                attachedLocationsCollectionNew.add(locationsCollectionNewLocationsToAttach);
            }
            locationsCollectionNew = attachedLocationsCollectionNew;
            countries.setLocationsCollection(locationsCollectionNew);
            countries = em.merge(countries);
            if (regionIdOld != null && !regionIdOld.equals(regionIdNew)) {
                regionIdOld.getCountriesCollection().remove(countries);
                regionIdOld = em.merge(regionIdOld);
            }
            if (regionIdNew != null && !regionIdNew.equals(regionIdOld)) {
                regionIdNew.getCountriesCollection().add(countries);
                regionIdNew = em.merge(regionIdNew);
            }
            for (Locations locationsCollectionNewLocations : locationsCollectionNew) {
                if (!locationsCollectionOld.contains(locationsCollectionNewLocations)) {
                    Countries oldCountryIdOfLocationsCollectionNewLocations = locationsCollectionNewLocations.getCountryId();
                    locationsCollectionNewLocations.setCountryId(countries);
                    locationsCollectionNewLocations = em.merge(locationsCollectionNewLocations);
                    if (oldCountryIdOfLocationsCollectionNewLocations != null && !oldCountryIdOfLocationsCollectionNewLocations.equals(countries)) {
                        oldCountryIdOfLocationsCollectionNewLocations.getLocationsCollection().remove(locationsCollectionNewLocations);
                        oldCountryIdOfLocationsCollectionNewLocations = em.merge(oldCountryIdOfLocationsCollectionNewLocations);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = countries.getCountryId();
                if (findCountries(id) == null) {
                    throw new NonexistentEntityException("The countries with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Countries countries;
            try {
                countries = em.getReference(Countries.class, id);
                countries.getCountryId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The countries with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Locations> locationsCollectionOrphanCheck = countries.getLocationsCollection();
            for (Locations locationsCollectionOrphanCheckLocations : locationsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Countries (" + countries + ") cannot be destroyed since the Locations " + locationsCollectionOrphanCheckLocations + " in its locationsCollection field has a non-nullable countryId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Regions regionId = countries.getRegionId();
            if (regionId != null) {
                regionId.getCountriesCollection().remove(countries);
                regionId = em.merge(regionId);
            }
            em.remove(countries);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Countries> findCountriesEntities() {
        return findCountriesEntities(true, -1, -1);
    }

    public List<Countries> findCountriesEntities(int maxResults, int firstResult) {
        return findCountriesEntities(false, maxResults, firstResult);
    }

    private List<Countries> findCountriesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Countries.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Countries findCountries(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Countries.class, id);
        } finally {
            em.close();
        }
    }

    public int getCountriesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Countries> rt = cq.from(Countries.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
