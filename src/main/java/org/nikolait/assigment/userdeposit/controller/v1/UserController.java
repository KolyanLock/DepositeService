package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@SecurityRequirement(name = AUTHORIZATION)
public class UserController {

    private final SearchService searchService;

    @GetMapping("/search")
    public Page<UserEs> searchUsers(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String phone,
                                    @RequestParam(required = false) LocalDate dateOfBirth,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return searchService.searchUsers(name, email, phone, dateOfBirth, PageRequest.of(page, size));
    }

    @GetMapping("/me")
    public UserEs getCurrentUserBasic() {
        return searchService.getUserBasic(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/me/full")
    public UserEs getCurrentUserFull() {
        return searchService.getUserFull(SecurityUtils.getCurrentUserId());
    }
}
