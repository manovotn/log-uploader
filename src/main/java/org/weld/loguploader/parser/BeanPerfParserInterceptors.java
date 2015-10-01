/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.weld.loguploader.parser;

import java.util.List;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class BeanPerfParserInterceptors extends BeanPerfParser {

    public BeanPerfParserInterceptors(String pathToLog, List<String> tagList, String buildNameAndNumber) {
        super(pathToLog, tagList, buildNameAndNumber);
    }

    @Override
    protected String getUid() {
        return "weld_bean_perf_interceptors";
    }

}
