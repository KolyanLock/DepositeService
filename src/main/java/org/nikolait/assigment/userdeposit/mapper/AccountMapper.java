package org.nikolait.assigment.userdeposit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assigment.userdeposit.dto.AccountResponse;
import org.nikolait.assigment.userdeposit.entity.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountResponse toResponse(Account account);

}
