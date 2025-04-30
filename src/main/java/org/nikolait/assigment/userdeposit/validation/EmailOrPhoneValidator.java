package org.nikolait.assigment.userdeposit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import static org.nikolait.assigment.userdeposit.validation.ValidationPatterns.PHONE_PATTERN;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhone, String> {

    private final EmailValidator emailValidator = new EmailValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return emailValidator.isValid(value, context) || PHONE_PATTERN.matcher(value).matches();
    }

}
