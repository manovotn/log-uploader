/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perfrepo.model.builder.TestExecutionBuilder;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class NumberguessPerfParser extends GeneralParser {

    private static final String ITERATION = "iteration";
    private static final String MAX_TIME = "max response time";
    private static final String MEAN_TIME = "mean response time";
    private static final String SAMPLES = "samples";
    private static final String SESSIONS = "sessions";
    private static final String THROUGHPUT = "throughput";

    private List<Map<String, Double>> storedValues;

    public NumberguessPerfParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    /**
     *
     * Values for numberguess testing are multi value based. Each line of the log file has to be parsed and inserted as one one.
     * Following values are sought after: throughput, mean time, max time, samples, sessions and iteration number (derived).
     * Iteration number is used as a baseline for comparisons - each value is listed against # iteration -> value("throughput",
     * 800.0, "iteration", "1")
     *
     */
    @Override
    protected void setValues(TestExecutionBuilder builder) throws IOException {
        for (Map m : storedValues) {
            builder.value(THROUGHPUT, (Double) m.get(THROUGHPUT), ITERATION, String.valueOf(storedValues.indexOf(m)));
            builder.value(MEAN_TIME, (Double) m.get(MEAN_TIME), ITERATION, String.valueOf(storedValues.indexOf(m)));
            builder.value(MAX_TIME, (Double) m.get(MAX_TIME), ITERATION, String.valueOf(storedValues.indexOf(m)));
            builder.value(SAMPLES, (Double) m.get(SAMPLES), ITERATION, String.valueOf(storedValues.indexOf(m)));
            builder.value(SESSIONS, (Double) m.get(SESSIONS), ITERATION, String.valueOf(storedValues.indexOf(m)));
        }
    }

    /**
     * Each line must be verified not to contain sampling errors (last few can contain them) and unhealthy samples. Amount of
     * such lines has to be satisfying. When parsing reaches first such error, we can already assume on the output.
     *
     * Parsing stores the values for upload so that setvalues() method can only feed them to parser.
     */
    @Override
    protected void verifyLogMakesSense() throws IOException {
        storedValues = new ArrayList<>();

        // patterns to match for wanted information
        Pattern throughtput = Pattern.compile("throughput: [0-9]*[,]*[0-9]+\\.[0-9]+");
        Pattern meanTime = Pattern.compile("mean: [0-9]+");
        Pattern maxTime = Pattern.compile("max: [0-9]+");
        Pattern samples = Pattern.compile("samples: [0-9]+");
        Pattern sessions = Pattern.compile("Sessions: [0-9]+");

        //patterns to match for possible errors, if these are found then there are no errors
        Pattern noUnhealthySamples = Pattern.compile("unhealthy samples: 0,");
        Pattern noSamplingErrors = Pattern.compile("sampling errors: 0,");
        Pattern noSampleInvalid = Pattern.compile("100%");

        int validLines = 0;
        String oneLine;
        while ((oneLine = reader.readLine()) != null) {
            // verify if the line is valid, if so, then store values
            if (noUnhealthySamples.matcher(oneLine).find() && noSamplingErrors.matcher(oneLine).find() && noSampleInvalid.matcher(oneLine).find()) {
                // for each valid line increase the counter and save line info
                validLines++;
                Map<String, Double> wantedData = new HashMap<>();

                // prepare matchers and fire them
                Matcher throughtputMatcher = throughtput.matcher(oneLine);
                Matcher meanTimeMatcher = meanTime.matcher(oneLine);
                Matcher maxTimeMatcher = maxTime.matcher(oneLine);
                Matcher samplesMatcher = samples.matcher(oneLine);
                Matcher sessionsMatcher = sessions.matcher(oneLine);

                throughtputMatcher.find();
                meanTimeMatcher.find();
                maxTimeMatcher.find();
                samplesMatcher.find();
                sessionsMatcher.find();

                wantedData.put(THROUGHPUT, Double.valueOf(throughtputMatcher.group(0).substring(12).replace(",", "")));
                wantedData.put(MEAN_TIME, Double.valueOf(meanTimeMatcher.group(0).substring(5)));
                wantedData.put(MAX_TIME, Double.valueOf(maxTimeMatcher.group(0).substring(4)));
                wantedData.put(SAMPLES, Double.valueOf(samplesMatcher.group(0).substring(8)));
                wantedData.put(SESSIONS, Double.valueOf(sessionsMatcher.group(0).substring(9)));

                storedValues.add(wantedData);
            } else {
                // unhealthy sample detected, exit loop
                break;
            }
        }
        // close resource
        closeResource();

        // decide whether the log had sufficient amount of lines
        if (validLines < 10) {
            throw new IllegalArgumentException("The log only had " + validLines + " valid lines. That is not enough!");
        }
    }
}
