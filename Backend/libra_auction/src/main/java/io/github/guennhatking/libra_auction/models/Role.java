package io.github.guennhatking.libra_auction.models;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class Role {
    @Id
    private String name;

    private String description;

    @ManyToMany
    private Set<Permission> permissions;

    // CONSTRUCTOR
    public Role() {
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // GETTER
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    // SETTER
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
