package com.o19s.solr.search;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;

/**
 * Created by doug on 8/10/16.
 *
 * This query parser is inspired by the "field" query parser in that it
 * consults the query-side analyzer to perform
 */
public class MatchQParserPlugin extends QParserPlugin {


    public QParser createParser(String s, SolrParams localParams,
                                SolrParams globalPraams, SolrQueryRequest solrQueryRequest) {
        return new MatchQParser(s, localParams, globalPraams, solrQueryRequest);
    }
}
