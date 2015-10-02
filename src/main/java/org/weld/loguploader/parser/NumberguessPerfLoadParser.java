/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class NumberguessPerfLoadParser extends NumberguessPerfParser {

    public NumberguessPerfLoadParser(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    @Override
    protected String getUid() {
        return "weld_numberguess_perf_load";
    }

}
