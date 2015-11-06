/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser.beans;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perfrepo.model.builder.TestExecutionBuilder;
import org.weld.loguploader.parser.GeneralParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class BeanPerfParser extends GeneralParser {

    public BeanPerfParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    /**
     * With bean deployment time we care only about deployment time
     */
    @Override
    protected void setValues(TestExecutionBuilder builder) throws IOException {
        String oneLine;
        while ((oneLine = reader.readLine()) != null) {
            if (oneLine.startsWith("Deployment time")) {
                Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+");
                Matcher matcher = pattern.matcher(oneLine);
                matcher.find();
                // group 0 inside matcher now contains the proper part of the string
                builder.value("deployTimeInSec", Double.valueOf(matcher.group(0)));
            }
        }
    }

    @Override
    protected void verifyLogMakesSense() throws IOException {
        // verify no errors were present and all samples were valid
        Pattern errors = Pattern.compile("errors:\\s[0-9]+");
        Pattern validSamples = Pattern.compile("[0-9]+%");

        String firstLine = reader.readLine();

        Matcher matcherForErrors = errors.matcher(firstLine);
        matcherForErrors.find();

        Matcher matcherForSamples = validSamples.matcher(firstLine);
        matcherForSamples.find();

        // verify
        if ((!matcherForErrors.group(0).substring(8).equals("0")) || (!matcherForSamples.group(0).equals("100%"))) {
            throw new IOException("Log file is corrupted, it contains errors an/or invalid samples, please check!");
        } else {
            // return reader to the beginning of the file
            manager.closeResource();
            reader = manager.getReader();
        }
    }

}
