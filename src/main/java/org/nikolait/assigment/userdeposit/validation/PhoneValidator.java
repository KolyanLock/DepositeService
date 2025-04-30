package org.nikolait.assigment.userdeposit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.nikolait.assigment.userdeposit.validation.ValidationPatterns.PHONE_PATTERN;

public class PhoneValidator implements ConstraintValidator<Phone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
