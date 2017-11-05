/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.gestatech.hr.ejb.domain.controler;

import be.gestatech.hr.ejb.domain.controler.exceptions.NonexistentEntityException;
import be.gestatech.hr.ejb.domain.controler.exceptions.PreexistingEntityException;
import be.gestatech.hr.ejb.domain.controler.exceptions.RollbackFailureException;
import be.gestatech.hr.ejb.domain.entity.Departments;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import be.gestatech.hr.ejb.domain.entity.Employees;
import be.gestatech.hr.ejb.domain.entity.Locations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author amuri
 */
public class DepartmentsJpaController implements Serializable {

    public DepartmentsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Departments departments) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (departments.getEmployeesCollection() == null) {
            departments.setEmployeesCollection(new ArrayList<Employees>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Employees managerId = departments.getManagerId();
            if (managerId != null) {
                managerId = em.getReference(managerId.getClass(), managerId.getEmployeeId());
                departments.setManagerId(managerId);
            }
            Locations locationId = departments.getLocationId();
            if (locationId != null) {
                locationId = em.getReference(locationId.getClass(), locationId.getLocationId());
                departments.setLocationId(locationId);
            }
            Collection<Employees> attachedEmployeesCollection = new ArrayList<Employees>();
            for (Employees employeesCollectionEmployeesToAttach : departments.getEmployeesCollection()) {
                employeesCollectionEmployeesToAttach = em.getReference(employeesCollectionEmployeesToAttach.getClass(), employeesCollectionEmployeesToAttach.getEmployeeId());
                attachedEmployeesCollection.add(employeesCollectionEmployeesToAttach);
            }
            departments.setEmployeesCollection(attachedEmployeesCollection);
            em.persist(departments);
            if (managerId != null) {
                managerId.getDepartmentsCollection().add(departments);
                managerId = em.merge(managerId);
            }
            if (locationId != null) {
                locationId.getDepartmentsCollection().add(departments);
                locationId = em.merge(locationId);
            }
            for (Employees employeesCollectionEmployees : departments.getEmployeesCollection()) {
                Departments oldDepartmentIdOfEmployeesCollectionEmployees = employeesCollectionEmployees.getDepartmentId();
                employeesCollectionEmployees.setDepartmentId(departments);
                employeesCollectionEmployees = em.merge(employeesCollectionEmployees);
                if (oldDepartmentIdOfEmployeesCollectionEmployees != null) {
                    oldDepartmentIdOfEmployeesCollectionEmployees.getEmployeesCollection().remove(employeesCollectionEmployees);
                    oldDepartmentIdOfEmployeesCollectionEmployees = em.merge(oldDepartmentIdOfEmployeesCollectionEmployees);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findDepartments(departments.getDepartmentId()) != null) {
                throw new PreexistingEntityException("Departments " + departments + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Departments departments) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Departments persistentDepartments = em.find(Departments.class, departments.getDepartmentId());
            Employees managerIdOld = persistentDepartments.getManagerId();
            Employees managerIdNew = departments.getManagerId();
            Locations locationIdOld = persistentDepartments.getLocationId();
            Locations locationIdNew = departments.getLocationId();
            Collection<Employees> employeesCollectionOld = persistentDepartments.getEmployeesCollection();
            Collection<Employees> employeesCollectionNew = departments.getEmployeesCollection();
            if (managerIdNew != null) {
                managerIdNew = em.getReference(managerIdNew.getClass(), managerIdNew.getEmployeeId());
                departments.setManagerId(managerIdNew);
            }
            if (locationIdNew != null) {
                locationIdNew = em.getReference(locationIdNew.getClass(), locationIdNew.getLocationId());
                departments.setLocationId(locationIdNew);
            }
            Collection<Employees> attachedEmployeesCollectionNew = new ArrayList<Employees>();
            for (Employees employeesCollectionNewEmployeesToAttach : employeesCollectionNew) {
                employeesCollectionNewEmployeesToAttach = em.getReference(employeesCollectionNewEmployeesToAttach.getClass(), employeesCollectionNewEmployeesToAttach.getEmployeeId());
                attachedEmployeesCollectionNew.add(employeesCollectionNewEmployeesToAttach);
            }
            employeesCollectionNew = attachedEmployeesCollectionNew;
            departments.setEmployeesCollection(employeesCollectionNew);
            departments = em.merge(departments);
            if (managerIdOld != null && !managerIdOld.equals(managerIdNew)) {
                managerIdOld.getDepartmentsCollection().remove(departments);
                managerIdOld = em.merge(managerIdOld);
            }
            if (managerIdNew != null && !managerIdNew.equals(managerIdOld)) {
                managerIdNew.getDepartmentsCollection().add(departments);
                managerIdNew = em.merge(managerIdNew);
            }
            if (locationIdOld != null && !locationIdOld.equals(locationIdNew)) {
                locationIdOld.getDepartmentsCollection().remove(departments);
                locationIdOld = em.merge(locationIdOld);
            }
            if (locationIdNew != null && !locationIdNew.equals(locationIdOld)) {
                locationIdNew.getDepartmentsCollection().add(departments);
                locationIdNew = em.merge(locationIdNew);
            }
            for (Employees employeesCollectionOldEmployees : employeesCollectionOld) {
                if (!employeesCollectionNew.contains(employeesCollectionOldEmployees)) {
                    employeesCollectionOldEmployees.setDepartmentId(null);
                    employeesCollectionOldEmployees = em.merge(employeesCollectionOldEmployees);
                }
            }
            for (Employees employeesCollectionNewEmployees : employeesCollectionNew) {
                if (!employeesCollectionOld.contains(employeesCollectionNewEmployees)) {
                    Departments oldDepartmentIdOfEmployeesCollectionNewEmployees = employeesCollectionNewEmployees.getDepartmentId();
                    employeesCollectionNewEmployees.setDepartmentId(departments);
                    employeesCollectionNewEmployees = em.merge(employeesCollectionNewEmployees);
                    if (oldDepartmentIdOfEmployeesCollectionNewEmployees != null && !oldDepartmentIdOfEmployeesCollectionNewEmployees.equals(departments)) {
                        oldDepartmentIdOfEmployeesCollectionNewEmployees.getEmployeesCollection().remove(employeesCollectionNewEmployees);
                        oldDepartmentIdOfEmployeesCollectionNewEmployees = em.merge(oldDepartmentIdOfEmployeesCollectionNewEmployees);
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
                Integer id = departments.getDepartmentId();
                if (findDepartments(id) == null) {
                    throw new NonexistentEntityException("The departments with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Departments departments;
            try {
                departments = em.getReference(Departments.class, id);
                departments.getDepartmentId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The departments with id " + id + " no longer exists.", enfe);
            }
            Employees managerId = departments.getManagerId();
            if (managerId != null) {
                managerId.getDepartmentsCollection().remove(departments);
                managerId = em.merge(managerId);
            }
            Locations locationId = departments.getLocationId();
            if (locationId != null) {
                locationId.getDepartmentsCollection().remove(departments);
                locationId = em.merge(locationId);
            }
            Collection<Employees> employeesCollection = departments.getEmployeesCollection();
            for (Employees employeesCollectionEmployees : employeesCollection) {
                employeesCollectionEmployees.setDepartmentId(null);
                employeesCollectionEmployees = em.merge(employeesCollectionEmployees);
            }
            em.remove(departments);
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

    public List<Departments> findDepartmentsEntities() {
        return findDepartmentsEntities(true, -1, -1);
    }

    public List<Departments> findDepartmentsEntities(int maxResults, int firstResult) {
        return findDepartmentsEntities(false, maxResults, firstResult);
    }

    private List<Departments> findDepartmentsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Departments.class));
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

    public Departments findDepartments(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Departments.class, id);
        } finally {
            em.close();
        }
    }

    public int getDepartmentsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Departments> rt = cq.from(Departments.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
