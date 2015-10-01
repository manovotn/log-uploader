/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.perfrepo.client.PerfRepoClient;
import org.perfrepo.model.TestExecution;
import org.weld.loguploader.parser.BeanPerfParserDecorators;
import org.weld.loguploader.parser.BeanPerfParserEvents;
import org.weld.loguploader.parser.BeanPerfParserInterceptors;
import org.weld.loguploader.parser.BeanPerfParserProducers;
import org.weld.loguploader.parser.BeanPerfParserSimpleInjection;
import org.weld.loguploader.parser.GeneralParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class UploaderMain {

    public static final String LOGIN = "manovotn";
    public static final String PASSWORD = "";

    /**
     * First param is path to the log file. Second param is the list of tags, separated by commas, e.g. -> weld,perf,jenkins
     * Third param is build name and number, obtained via Jenkins variables
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
// parse input args, must contain path to log file
        String pathToLogFile;
        List<String> tagList;
        String buildNameAndNumber;

        if (args.length > 2) {
            pathToLogFile = args[0];
            tagList = new ArrayList<>(Arrays.asList(args[1].split(",")));
            buildNameAndNumber = args[2];
        } else {
            throw new IllegalStateException("Wrong amount of input parameters!");
        }

        GeneralParser parser = null;
        // detect what parser to use
        //TODO add parsers for stress/load tests
        String switchStatement = buildNameAndNumber.substring(0, buildNameAndNumber.lastIndexOf("-")).trim();
        switch (switchStatement) {
            case "jenkins-eap-7x-Weld-perf-bean-testing-decorators":
                parser = new BeanPerfParserDecorators(pathToLogFile, tagList, buildNameAndNumber);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-events":
                parser = new BeanPerfParserEvents(pathToLogFile, tagList, buildNameAndNumber);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-interceptors":
                parser = new BeanPerfParserInterceptors(pathToLogFile, tagList, buildNameAndNumber);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-producers":
                parser = new BeanPerfParserProducers(pathToLogFile, tagList, buildNameAndNumber);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-simpleinjection":
                parser = new BeanPerfParserSimpleInjection(pathToLogFile, tagList, buildNameAndNumber);
                break;
            default:
                System.err.println("FAILURE: no suitable parser can be used.");
                System.exit(1);
        }

        // verify of the log is correct
        parser.verifyLog();

        TestExecution testExecution = null;
        try {
            testExecution = parser.createTestExecution();
        } catch (IOException e) {
            System.err.println("Caught IOException, closing resources and exiting...");
            parser.closeResource();
            System.exit(1);
        }

        // close the resource
        parser.closeResource();

        // create client
        String host = "localhost:8080";
        PerfRepoClient client = new PerfRepoClient(host, "", LOGIN, PASSWORD);

        try {
            //upload
            client.createTestExecution(testExecution);
        } catch (Exception ex) {
            // shouldnt happen
            System.out.println("Problem while uploading file, original message: " + ex.getMessage());
        }
    }

}
