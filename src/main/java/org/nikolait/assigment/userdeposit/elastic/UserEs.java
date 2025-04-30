package org.nikolait.assigment.userdeposit.elastic;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(indexName = "user")
public class UserEs {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Date)
    private LocalDate dateOfBirth;

    @Field(type = FieldType.Nested)
    private List<EmailDataEs> emails;

    @Field(type = FieldType.Nested)
    private List<PhoneDataEs> phones;

}
