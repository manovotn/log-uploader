package org.weld.loguploader.parser.numberguess.load;

import java.util.List;
import java.util.Map;

import org.weld.loguploader.parser.numberguess.NumberguessPerfParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class NumberguessPerfLoadHAParser extends NumberguessPerfParser {

    public NumberguessPerfLoadHAParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    @Override
    protected int getNumberOfValidLines() {
        return 5;
    }
}
