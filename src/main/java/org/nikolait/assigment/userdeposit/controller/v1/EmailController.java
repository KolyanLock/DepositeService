package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.EmailRequest;
import org.nikolait.assigment.userdeposit.elastic.EmailDataEs;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.EmailDataService;
import org.nikolait.assigment.userdeposit.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/email")
@SecurityRequirement(name = AUTHORIZATION)
public class EmailController {

    private final EmailDataService emailDataService;
    private final SearchService searchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmail(@RequestBody @Valid EmailRequest request) {
        emailDataService.createEmail(request.getEmail());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEmail(@PathVariable Long id,
                            @RequestBody @Valid EmailRequest request) {
        emailDataService.updateEmail(id, request.getEmail());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmail(@PathVariable Long id) {
        emailDataService.deleteEmail(id);
    }

    @GetMapping
    public List<EmailDataEs> getUserEmails() {
        return searchService.getUserEmails(SecurityUtils.getCurrentUserId());
    }

}
