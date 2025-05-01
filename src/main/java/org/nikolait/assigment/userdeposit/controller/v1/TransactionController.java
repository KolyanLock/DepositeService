package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.dto.TransactionRequest;
import org.nikolait.assigment.userdeposit.dto.TransactionResponse;
import org.nikolait.assigment.userdeposit.dto.TransferRequest;
import org.nikolait.assigment.userdeposit.entity.Transaction;
import org.nikolait.assigment.userdeposit.mapper.TransactionMapper;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
@SecurityRequirement(name = AUTHORIZATION)
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping("/transfer/init")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse initTransfer(@RequestBody @Valid TransferRequest request) {
        Long fromUserId = SecurityUtils.getCurrentUserId();
        Transaction transaction = transactionService.initTransfer(fromUserId, request.getUserId(), request.getValue());
        return transactionMapper.toResponse(transaction);
    }

    @PostMapping("/transfer/commit")
    public TransactionResponse commitTransfer(@RequestBody @Valid TransactionRequest request) {
        try {
            Long fromUserId = SecurityUtils.getCurrentUserId();
            Transaction transaction = transactionService.commitTransfer(request.getId(), fromUserId);
            return transactionMapper.toResponse(transaction);
        } catch (Exception e) {
            log.error("Executing transaction {} failed! Error: {}, message: {}",
                    request.getId(), e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

}
