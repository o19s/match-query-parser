package com.o19s.solr.search;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * Created by doug on 8/10/16.
 */
public class MatchQParserPlugin extends QParserPlugin {
    public QParser createParser(String s, SolrParams solrParams, SolrParams solrParams1, SolrQueryRequest solrQueryRequest) {
        return null;
    }
}
