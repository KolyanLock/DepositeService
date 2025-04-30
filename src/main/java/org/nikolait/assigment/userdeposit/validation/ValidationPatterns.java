package org.nikolait.assigment.userdeposit.validation;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationPatterns {
    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,13}$");
}
