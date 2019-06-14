/*
 */
package com.artezio.example.billling.adaptor.web.validators;

import java.math.BigDecimal;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Payment processing tries limit JSF validation bean.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@RequestScope
@Component
public class LimitValidator implements Validator {

    /**
     *  Payment processing tries limit JSF validation implementation.
     *
     * @param fc JSF context access object.
     * @param uic User interface control object.
     * @param o Validation object.
     * @throws ValidatorException @see javax.faces.validator.ValidatorException
     */
    @Override
    public void validate(FacesContext fc, UIComponent uic, Object o) throws ValidatorException {
        if (o == null) {
            return;
        }
        String value = String.valueOf(o).trim();
        if (value.isEmpty()) {
            return;
        }
        if (!value.matches("[0-9]+")) {
            FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        } else {
            BigDecimal amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) < 0
                    || amount.compareTo(new BigDecimal(10)) > 0) {
                FacesMessage msg = new FacesMessage();
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(msg);
            }
        }
    }

}
