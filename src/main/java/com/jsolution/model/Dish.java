package com.jsolution.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "dishes")
public class Dish {

    @EqualsAndHashCode.Include
    @Id
    private String id;

    @Size(min = 3)
    @Field(name = "name")
    private String name;
    @Field(name = "price")
    private Double price;
    @Field(name = "status")
    private Boolean status;
}
