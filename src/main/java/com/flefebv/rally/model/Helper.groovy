package com.flefebv.rally.model;

import com.flefebv.rally.jetbrains.tasks.RallyRepository;
import com.intellij.openapi.diagnostic.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
public class Helper {

    private static final Logger LOG = Logger.getInstance(RallyRepository.class);

    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

    public static Date toDate (String dateStr) {
        try {
            return DF.parse(dateStr);
        } catch (ParseException e) {
            LOG.warn("Date parse error", e);
            return null;
        }
    }

    public static void main (String[] args) {
        System.out.println(Helper.toDate("2015-04-23T12:44:48.130Z"));
    }
}
