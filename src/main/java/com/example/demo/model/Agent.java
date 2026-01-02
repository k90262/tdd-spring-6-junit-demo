package com.example.demo.model;

import java.util.Objects;

public class Agent {

    private Long id;

    private String name;

    public Agent() {}

    public Agent(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id) && Objects.equals(name, agent.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
