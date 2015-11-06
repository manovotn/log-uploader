/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perfrepo.model.TestExecution;
import org.perfrepo.model.builder.TestExecutionBuilder;
import org.weld.loguploader.filemanagement.FileManager;

/**
 * General parser abstract class. Allows to verify path to the file and defines abstract methods to verify its contents.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class GeneralParser {

    protected FileManager manager;
    protected String pathToLog;
    protected BufferedReader reader;
    protected List<String> tagList;
    protected String comment;
    protected Map<String, String> params;

    // result set of test executions to be uploaded
    protected Set<TestExecution> allTestExecutions = new HashSet<>();
    
    public GeneralParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        this.pathToLog = pathToLog;
        this.tagList = tagList;
        this.params = params;
        this.comment = comment;
    }

    public void closeResource() {
        manager.closeResource();
    }

    public Set<TestExecution> createTestExecutions() throws IOException {
        parseLogToTestExecutions();
        return Collections.unmodifiableSet(allTestExecutions);
    }

    /**
     * Responsible for adding creating TestExecutions and adding 
     * them to a local result-defining set (variable named allTestExecutions)
     * 
     * @return actual state of the set when this method ends
     * @throws IOException should anything go wrong
     */
    protected void parseLogToTestExecutions() throws IOException {
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
        // e.g. Weld, perf. bean-testing, events/decorators/interceptors,producers/simple-injection
        for (String s : tagList) {
            builder.tag(s);
        }

        // add params
        for (String key : params.keySet()) {
            builder.parameter(key, params.get(key));
        }
        // build number is extracted from build tag and added as separate param for readability
        builder.parameter("Build number", getBuildNumber());

        // values, each parser instance implements this separately
        setValues(builder);

        // comment about build, ignore when it is default, comments are not mandatory
        // TODO could be used to determine re-run?
        if (!comment.equalsIgnoreCase("Dummy default comment")) {
            builder.comment(comment);
        }

        // finish by creating TestExecution and adding it to set of executions to be uploaded
        allTestExecutions.add(builder.build());
    }

    protected abstract String getUid();

    protected String getBuildName() {
        return params.get("BUILD_TAG");
    }

    protected String getBuildNumber() {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(getBuildName());
        matcher.find();
        return matcher.group(0);
    }

    public void verifyLog() {
        try {
            verifyLogExists();
            verifyLogIsNotEmpty();
            verifyLogMakesSense(); //test specific, has to be done in separate classes
        } catch (IOException e) {
            System.err.println("IO error occured, attempting to close resources and exiting. Original cause was: " + e.getMessage());
            manager.closeResource();
        }
    }

    protected void verifyLogExists() throws IOException {
        manager = new FileManager(pathToLog);
        reader = manager.getReader();
    }

    protected abstract void setValues(TestExecutionBuilder builder) throws IOException;

    protected void verifyLogIsNotEmpty() throws IOException {
        reader.mark(500);
        String testLine = reader.readLine();
        // detects empty log file
        if (testLine == null || testLine.isEmpty()) {
            reader.close();
            System.err.println("Provided log file is empty! Exiting ...");
            System.exit(0);
        } else {
            // move back to the beginning of the log
            reader.reset();
        }

    }

    protected abstract void verifyLogMakesSense() throws IOException;
}
