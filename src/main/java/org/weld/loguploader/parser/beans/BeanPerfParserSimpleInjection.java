/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser.beans;

import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class BeanPerfParserSimpleInjection extends BeanPerfParser {

    public BeanPerfParserSimpleInjection(String pathToLog, List<String> tagList, Map<String, String> params, String comment) {
        super(pathToLog, tagList, params, comment);
    }

    @Override
    protected String getUid() {
        return "weld_bean_perf_simple_injection";
    }

}
