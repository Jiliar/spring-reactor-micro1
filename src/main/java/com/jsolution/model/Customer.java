package com.jsolution.model;

import java.time.LocalDate;

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
@Document(collection = "customers")
public class Customer {

    @EqualsAndHashCode.Include
    @Id
    private String id;
    @Size(max = 100)
    @Field(name = "names")
    private String names;

    @Size(max = 100)
    @Field(name = "lastnames")
    private String lastnames;

    @Field(name = "birthday")
    private LocalDate birthday;

    @Field(name = "url_picture")
    private String url_picture;

}
