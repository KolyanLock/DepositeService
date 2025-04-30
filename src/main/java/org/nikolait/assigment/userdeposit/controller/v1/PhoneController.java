package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.PhoneRequest;
import org.nikolait.assigment.userdeposit.elastic.PhoneDataEs;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.PhoneDataService;
import org.nikolait.assigment.userdeposit.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/phone")
@SecurityRequirement(name = AUTHORIZATION)
public class PhoneController {

    private final PhoneDataService phoneDataService;
    private final SearchService searchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addPhone(@RequestBody @Valid PhoneRequest request) {
        phoneDataService.createPhone(request.getPhone());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePhone(@PathVariable Long id,
                            @RequestBody @Valid PhoneRequest request) {
        phoneDataService.updatePhone(id, request.getPhone());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhone(@PathVariable Long id) {
        phoneDataService.deletePhone(id);
    }

    @GetMapping
    public List<PhoneDataEs> getUserPhones() {
        return searchService.getUserPhones(SecurityUtils.getCurrentUserId());
    }

}
