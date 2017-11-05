/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.gestatech.hr.ejb.domain.entity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author amuri
 */
@Entity
@Table(catalog = "hr_dev", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Countries.findAll", query = "SELECT c FROM Countries c")
    , @NamedQuery(name = "Countries.findByCountryId", query = "SELECT c FROM Countries c WHERE c.countryId = :countryId")
    , @NamedQuery(name = "Countries.findByCountryName", query = "SELECT c FROM Countries c WHERE c.countryName = :countryName")})
public class Countries implements Serializable {

    private static final long serialVersionUID = -8696060496073849172L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2)
    @Column(name = "country_id", nullable = false, length = 2)
    private String countryId;
    @Size(max = 40)
    @Column(name = "country_name", length = 40)
    private String countryName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "countryId", fetch = FetchType.EAGER)
    private Collection<Locations> locationsCollection;
    @JoinColumn(name = "region_id", referencedColumnName = "region_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Regions regionId;

    public Countries() {
    }

    public Countries(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @XmlTransient
    public Collection<Locations> getLocationsCollection() {
        return locationsCollection;
    }

    public void setLocationsCollection(Collection<Locations> locationsCollection) {
        this.locationsCollection = locationsCollection;
    }

    public Regions getRegionId() {
        return regionId;
    }

    public void setRegionId(Regions regionId) {
        this.regionId = regionId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (countryId != null ? countryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Countries)) {
            return false;
        }
        Countries other = (Countries) object;
        if ((this.countryId == null && other.countryId != null) || (this.countryId != null && !this.countryId.equals(other.countryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "be.gestatech.hr.ejb.domain.entity.Countries[ countryId=" + countryId + " ]";
    }

}
