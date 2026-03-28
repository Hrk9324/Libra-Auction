package io.github.guennhatking.libra_auction.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {

    private int min;

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {

        if (dob == null) return false;

        return Period.between(dob, LocalDate.now()).getYears() >= min;
    }
}