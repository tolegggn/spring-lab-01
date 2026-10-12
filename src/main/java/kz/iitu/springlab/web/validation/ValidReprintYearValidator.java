package kz.iitu.springlab.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.iitu.springlab.web.BookForm;

public class ValidReprintYearValidator implements ConstraintValidator<ValidReprintYear, BookForm> {

    @Override
    public boolean isValid(BookForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        Integer reprintYear = form.getReprintYear();
        // If reprintYear is not specified, it's optional and valid
        if (reprintYear == null) {
            return true;
        }

        // A reprint year must not precede the first edition year: reprintYear >= year
        return reprintYear >= form.getYear();
    }
}
