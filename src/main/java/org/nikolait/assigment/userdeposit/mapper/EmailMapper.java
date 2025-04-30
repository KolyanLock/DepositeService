package org.nikolait.assigment.userdeposit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assigment.userdeposit.elastic.EmailDataEs;
import org.nikolait.assigment.userdeposit.entity.EmailData;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmailMapper {

    EmailDataEs toEmailDataEs(EmailData email);

    List<EmailDataEs> toEmailDataEsList(Set<EmailData> emails);

}
