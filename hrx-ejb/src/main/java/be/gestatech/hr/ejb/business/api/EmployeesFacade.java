/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.gestatech.hr.ejb.business.api;

import be.gestatech.hr.ejb.domain.entity.Employees;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author amuri
 */
@Local
public interface EmployeesFacade {

    void create(Employees employees);

    void edit(Employees employees);

    void remove(Employees employees);

    Employees find(Object id);

    List<Employees> findAll();

    List<Employees> findRange(int[] range);

    int count();

}
