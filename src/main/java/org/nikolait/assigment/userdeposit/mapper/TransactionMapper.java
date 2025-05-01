package org.nikolait.assigment.userdeposit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assigment.userdeposit.dto.TransactionResponse;
import org.nikolait.assigment.userdeposit.entity.Transaction;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    TransactionResponse toResponse(Transaction transaction);
}
