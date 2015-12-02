package org.weld.loguploader.parser.numberguess.stress;

import java.util.List;
import java.util.Map;

import org.weld.loguploader.parser.numberguess.NumberguessPerfParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class NumberguessPerfStressParser extends NumberguessPerfParser {

    public NumberguessPerfStressParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    @Override
    protected int getNumberOfValidLines() {
        return 20;
    }

}
