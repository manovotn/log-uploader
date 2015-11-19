package org.weld.loguploader.parser.benchmark;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.perfrepo.model.TestExecution;
import org.perfrepo.model.builder.TestExecutionBuilder;
import org.weld.loguploader.parser.GeneralParser;

/**
 * Parses microbenchmark tests.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class BenchmarkPerfParser extends GeneralParser {

    public BenchmarkPerfParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    private Map<String, Double> storedScore; //stored as testName:score
    private Map<String, Double> storedScoreError; //stored as testName:scoreError

    @Override
    protected String getUid() {
        return "weld_micro_benchmarks";
    }

    @Override
    protected void setValues(TestExecutionBuilder builder) throws IOException {
        // no-op, not used in this scenario
        // TODO this needs to be refactor to avoid such hallow methods
    }

    @Override
    protected void verifyLogMakesSense() throws IOException {
        String testName;
        String score;
        String scoreError;

        boolean firstLine = false;

        // init maps to store values in
        storedScore = new HashMap<>();
        storedScoreError = new HashMap<>();

        String oneLine;
        while ((oneLine = reader.readLine()) != null) {
            if (oneLine.startsWith("\"Benchmark\"") && oneLine.contains("\"Score Error (99.9%)\"")) { // first line is present
                firstLine = !firstLine;
            } else {
                String[] splitByComma = oneLine.split(",");
                if (splitByComma.length != 7 || !(splitByComma[1].equals("\"thrpt\"")) || !(splitByComma[2].equals("4"))
                    || Double.valueOf(splitByComma[3]).intValue() != 100) { //line validation
                    closeResource();
                    throw new IllegalStateException("Log is not valid, one or more lines are corrupted!");
                }
                String[] secondSplit = splitByComma[0].split("\\.");
                testName = splitByComma[0].substring(splitByComma[0].indexOf(secondSplit[5]), splitByComma[0].indexOf(secondSplit[secondSplit.length - 1]) - 1);
                score = splitByComma[4];
                scoreError = splitByComma[5];
                storedScore.put(testName, Double.valueOf(score));
                storedScoreError.put(testName, Double.valueOf(scoreError));
            }
        }

        closeResource();

        // assert first line is present and results are correct (e.g. size of keyset in both maps)
        if (!(firstLine && storedScore.keySet().size() == 29 && storedScoreError.keySet().size() == 29)) {
            throw new IllegalStateException("Parsing failed, log format/data was incorrect!");
        }
    }

    /**
     * Override the default implementation to allow for creation of multiple TestExecutions
     *
     * @throws IOException
     */
    @Override
    protected void parseLogToTestExecutions() throws IOException {
        for (String benchmark : storedScore.keySet()) {
            TestExecutionBuilder builder = TestExecution.builder();

            // match with Test by UID
            // each parser has a unique one to match the test case
            builder.testUid(getUid());

            // name will be decided on benchmark in question!
            builder.name(benchmark + "-Weld-" + params.get("weld"));

            //build date
            builder.started(new Date());

            // tags
            for (String s : tagList) {
                builder.tag(s);
            }
            // add tag for benchmark
            builder.tag(benchmark);

            // add params
            for (String key : params.keySet()) {
                builder.parameter(key, params.get(key));
            }
            builder.parameter("testName", benchmark);

            // build number is extracted from build tag and added as separate param for readability
            builder.parameter("Build number", getBuildNumber());

            // values, each parser instance implements this separately
            builder.value("score", storedScore.get(benchmark));
            builder.value("scoreError", storedScoreError.get(benchmark));

            // comment about build, ignore when it is default, comments are not mandatory
            // TODO could be used to determine re-run?
            if (!comment.equalsIgnoreCase("Dummy default comment")) {
                builder.comment(comment);
            }

            // finish by creating TestExecution and adding it to set of executions to be uploaded
            allTestExecutions.add(builder.build());
        }
    }

}
