/*
 */
package com.artezio.example.billling.adaptor.web.convertors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.ValidatorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Input date and time converter.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@RequestScope
@Component
@Slf4j
public class DateConverter implements Converter {

    /**
     * Date and time input format.
     */
    private static final DateFormat LONG_DATE_IN = new SimpleDateFormat("dd.MM.yyyyHH:mm");
    /**
     * Date and time output format.
     */
    private static final DateFormat LONG_DATE_OUT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    /**
     * Time only input format.
     */
    private static final DateFormat TIME_ONLY = new SimpleDateFormat("HH:mm");

    /**
     * Convert a string to the date.
     * 
     * @param fc JSF context access object.
     * @param uic User interface control object.
     * @param string Date representation string object.
     * @return Date object.
     */
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        if (string == null) {
            return null;
        }
        String value = string.replaceAll("\\s+", "");
        if (value.isEmpty()) {
            return null;
        }
        Date date;
        try {
            if (value.length() <= 5) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                Date userTime = TIME_ONLY.parse(value);
                date = new Date(today.getTimeInMillis() 
                        + userTime.getTime() 
                        + TimeZone.getDefault().getRawOffset());
            } else {
                date = LONG_DATE_IN.parse(value);
            }
        } catch (ParseException e) {
            log.warn(e.getClass().getSimpleName()
                    + ": "
                    + String.valueOf(e.getMessage()));
            FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
        return date;
    }

    /**
     * Convert an date object to the string.
     * 
     * @param fc JSF context access object.
     * @param uic User interface control object.
     * @param o Date object.
     * @return Date representation string object.
     */
    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return (o instanceof Date)
                ? LONG_DATE_OUT.format((Date) o)
                : "";
    }

}
