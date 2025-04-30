package org.nikolait.assigment.userdeposit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.nikolait.assigment.userdeposit.entity.User;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {EmailMapper.class, PhoneMapper.class}
)
public interface UserMapper {

    UserEs toUserEs(User user);

    List<UserEs> toUserEsList(List<User> users);

}
