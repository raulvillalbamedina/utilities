package com.rvillalba.utilities;

import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocaleExample {

    @Test
    public void tableWithAllLocalesInfo() {
        List<Locale> list = Arrays.asList(DateFormat.getAvailableLocales());
        Date now = new Date();
        for (Locale locale : list) {
            System.out.println("<tr><td> Language - " + locale.getLanguage() + "</td><td> - Number format - "
                    + NumberFormat.getInstance(locale).format(12345.6789) + "</td><td> - dateShort - "
                    + DateFormat.getDateInstance(DateFormat.SHORT, locale).format(now) + "</td><td> - dateLong - "
                    + DateFormat.getDateInstance(DateFormat.LONG, locale).format(now) + "</td></tr>");
        }
    }

}
