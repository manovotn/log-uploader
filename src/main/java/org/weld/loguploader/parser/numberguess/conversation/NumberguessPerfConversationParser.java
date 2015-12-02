package org.weld.loguploader.parser.numberguess.conversation;

import java.util.List;
import java.util.Map;

import org.weld.loguploader.parser.numberguess.NumberguessPerfParser;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class NumberguessPerfConversationParser extends NumberguessPerfParser{

    public NumberguessPerfConversationParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    @Override
    protected int getNumberOfValidLines() {
        // for load test, there are 10 iterations, all should be valid
        // for stress test, there can be many more iterations (80+), 70 should be valid
        // detected by inspecting the BUILD_TAG param which has to be present
        return (params.get("BUILD_TAG").contains("stress")) ? 70 : 10;
    }
    
}
