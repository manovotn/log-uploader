/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perfrepo.model.TestExecution;
import org.perfrepo.model.builder.TestExecutionBuilder;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class BeanPerfParser extends GeneralParser {

    public BeanPerfParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    protected String getBuildName() {
        return params.get("BUILD_TAG");
    }

    protected String getBuildNumber() {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(getBuildName());
        matcher.find();
        return matcher.group(0);
    }

    protected abstract String getUid();

    @Override
    protected TestExecution parseLogToTestExecution() throws IOException {
        TestExecutionBuilder builder = TestExecution.builder();

        // match with Test by UID
        // each parser has a unique one to match the test case
        builder.testUid(getUid());

        // set a name, based on input par derived from jenkins
        builder.name(getBuildName());

        //build date
        builder.started(new Date());

        // tags need to be precise and derived from current job and EAP version
        // passed in as an argument to build for now
        //e.g. Weld, perf. bean-testing, events/decorators/interceptors,producers/simple-injection
        for (String s : tagList) {
            builder.tag(s);
        }

        //TODO find out what do we need this for
        for (String key : params.keySet()) {
            builder.parameter(key, params.get(key));
        }
        //build number is extracted from build tag and added as separate param for readability
        builder.parameter("Build number", getBuildNumber());

        // values, parsing is done via simple method
        setValues(builder);

        //comment about build, ignore when it is default, comments are not mandatory
        //TODO could be used to determine re-run?
        if (!comment.equalsIgnoreCase("Dummy default comment")) {
            builder.comment(comment);
        }

        //finish by creating TestExecution
        return builder.build();
    }

    /**
     * With bean deployment time we care only about deployment time
     */
    private void setValues(TestExecutionBuilder builder) throws IOException {
        String oneLine;
        while ((oneLine = reader.readLine()) != null) {
            if (oneLine.startsWith("Deployment time")) {
                Pattern pattern = Pattern.compile("[1-9]+\\.[1-9]+");
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
