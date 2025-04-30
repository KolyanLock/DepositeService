package org.nikolait.assigment.userdeposit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assigment.userdeposit.elastic.PhoneDataEs;
import org.nikolait.assigment.userdeposit.entity.PhoneData;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhoneMapper {

    PhoneDataEs toPhoneDataEs(PhoneData phone);

    List<PhoneDataEs> toPhoneDataEsList(Set<PhoneData> phones);

}
