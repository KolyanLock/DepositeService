package org.nikolait.assigment.userdeposit.validation;

import jakarta.validation.MessageInterpolator;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@RequiredArgsConstructor
public class FixedLocaleMessageInterpolator implements MessageInterpolator {

    private final MessageInterpolator defaultInterpolator;
    private final Locale fixedLocale;

    @Override
    public String interpolate(String s, Context context) {
        return defaultInterpolator.interpolate(s, context, fixedLocale);
    }

    @Override
    public String interpolate(String s, Context context, Locale locale) {
        return defaultInterpolator.interpolate(s, context, fixedLocale);
    }
}
