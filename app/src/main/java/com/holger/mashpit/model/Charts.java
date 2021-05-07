package com.holger.mashpit.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;


@Entity
public class Charts {
    @Id
    public long id;

    public String name;
    public String description;
    public String type;
}