/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.perfrepo.model.TestExecution;
import org.weld.loguploader.filemanagement.FileManager;

/**
 * General parser abstract class. Allows to verify path to the file and defines abstract methods to verify its contents.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class GeneralParser {

    protected String buildName;

    protected FileManager manager;
    protected String pathToLog;
    protected BufferedReader reader;
    protected List<String> tagList;

    public void closeResource() {
        manager.closeResource();
    }

    public TestExecution createTestExecution() throws IOException {
        return parseLogToTestExecution();
    }

    protected abstract TestExecution parseLogToTestExecution() throws IOException;

    public void verifyLog() {
        try {
            verifyLogExists();
            verifyLogIsNotEmpty();
            verifyLogMakesSense(); //test specific specific, must be done in separate classes
        } catch (IOException e) {
            System.err.println("IO error occured, attempting to close resources and exiting. Original cause was: " + e.getMessage());
            manager.closeResource();
        }
    }

    protected void verifyLogExists() throws IOException {
        manager = new FileManager(pathToLog);
        reader = manager.getReader();
    }

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
