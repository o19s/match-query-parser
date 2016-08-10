package com.o19s.solr.search;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

/**
 * Created by doug on 8/10/16.
 */
public class MatchQParser extends QParser {

    public MatchQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    public Query parse() throws SyntaxError {
        return null;
    }
}
