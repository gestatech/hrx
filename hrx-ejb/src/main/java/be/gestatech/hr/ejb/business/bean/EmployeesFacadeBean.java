/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.gestatech.hr.ejb.business.bean;

import be.gestatech.hr.ejb.business.api.AbstractFacade;
import be.gestatech.hr.ejb.domain.entity.Employees;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import be.gestatech.hr.ejb.business.api.EmployeesFacade;

/**
 *
 * @author amuri
 */
@Stateless
public class EmployeesFacadeBean extends AbstractFacade<Employees> implements EmployeesFacade {

    @PersistenceContext(unitName = "hrPersistenceUnit")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmployeesFacadeBean() {
        super(Employees.class);
    }

}
