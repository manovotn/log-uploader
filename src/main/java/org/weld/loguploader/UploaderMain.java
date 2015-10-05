/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.perfrepo.client.PerfRepoClient;
import org.perfrepo.model.TestExecution;
import org.weld.loguploader.parser.BeanPerfParserDecorators;
import org.weld.loguploader.parser.BeanPerfParserEvents;
import org.weld.loguploader.parser.BeanPerfParserInterceptors;
import org.weld.loguploader.parser.BeanPerfParserProducers;
import org.weld.loguploader.parser.BeanPerfParserSimpleInjection;
import org.weld.loguploader.parser.GeneralParser;
import org.weld.loguploader.parser.NumberguessPerfLoadHAParser;
import org.weld.loguploader.parser.NumberguessPerfLoadNoHAParser;
import org.weld.loguploader.parser.NumberguessPerfLoadParser;
import org.weld.loguploader.parser.NumberguessPerfLoadSyncHAParser;
import org.weld.loguploader.parser.NumberguessPerfStressHAParser;
import org.weld.loguploader.parser.NumberguessPerfStressNoHAParser;
import org.weld.loguploader.parser.NumberguessPerfStressParser;
import org.weld.loguploader.parser.NumberguessPerfStressSyncHAParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class UploaderMain {

    public static final String LOGIN = "fg";
    public static final String PASSWORD = "fg";
    public static final String urlAndPort = "somewhere:8080";

    /**
     * First param is path to the log file.
     *
     * Second param is the list of tags, separated by commas, e.g. -> weld,perf,jenkins
     *
     * Third param is build name and number, obtained via Jenkins variables
     *
     * repository address, login and password are passed as args in following form -> localhost:8080/user/userPass
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // parse input arg
        String pathToLogFile;
        String urlAndPort;
        String username;
        String password;
        List<String> tagList;
        Map<String, String> params;
        String comment;

        /**
         * Params are in the following order: path to log file; url; username; password; tags; params; comment
         *
         */
        if (args.length > 5) {
            pathToLogFile = args[0];
            urlAndPort = args[1];
            username = args[2];
            password = args[3];
            tagList = new ArrayList<>(Arrays.asList(args[4].split(",")));

            List<String> paramList = Arrays.asList(args[5].split(","));
            params = new HashMap<>();
            for (String keyValue : paramList) {
                String[] separatedKeyValue = keyValue.split("=");
                params.put(separatedKeyValue[0], separatedKeyValue[1]);
            }
            comment = args[6];

        } else {
            throw new IllegalStateException("Wrong amount of input parameters!");
        }

        GeneralParser parser = null;
        String buildNameAndNumber = params.get("BUILD_TAG");
        // detect what parser to use
        //TODO add parsers for stress/load tests
        String switchStatement = buildNameAndNumber.substring(0, buildNameAndNumber.lastIndexOf("-")).trim();
        switch (switchStatement) {
            case "jenkins-eap-7x-Weld-perf-bean-testing-decorators":
                parser = new BeanPerfParserDecorators(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-events":
                parser = new BeanPerfParserEvents(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-interceptors":
                parser = new BeanPerfParserInterceptors(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-producers":
                parser = new BeanPerfParserProducers(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-bean-testing-simpleinjection":
                parser = new BeanPerfParserSimpleInjection(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-load":
                parser = new NumberguessPerfLoadParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-load-cluster-noHA":
                parser = new NumberguessPerfLoadNoHAParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-load-cluster-sync-HA":
                parser = new NumberguessPerfLoadSyncHAParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-load-cluster-HA":
                parser = new NumberguessPerfLoadHAParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-stress-cluster-noHA":
                parser = new NumberguessPerfStressNoHAParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-stress":
                parser = new NumberguessPerfStressParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-stress-cluster-sync-HA":
                parser = new NumberguessPerfStressSyncHAParser(pathToLogFile, tagList, params, comment);
                break;
            case "jenkins-eap-7x-Weld-perf-numberguess-stress-cluster-HA":
                parser = new NumberguessPerfStressHAParser(pathToLogFile, tagList, params, comment);
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
        PerfRepoClient client = new PerfRepoClient(urlAndPort, "", username, password);

        try {
            //upload
            client.createTestExecution(testExecution);
        } catch (Exception ex) {
            // shouldnt happen
            System.out.println("Problem while uploading file, original message: " + ex.getMessage());
        }
    }

}
