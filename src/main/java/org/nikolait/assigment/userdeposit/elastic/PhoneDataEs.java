package org.nikolait.assigment.userdeposit.elastic;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class PhoneDataEs {

    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String phone;

}
