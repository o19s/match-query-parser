package com.o19s.solr.search;

import com.facebook.presto.sql.tree.Except;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Created by doug on 8/10/16.
 */
public class MatchQParserTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solrconfig.xml", "schema.xml", "target/test-classes/solr");
    }

    @Test
    public void testMatchQParser() throws SAXException {
        assertNull(h.validateUpdate(adoc("id", "1", "non_tok", "HELLO WORLD")));
        assertNull(h.validateUpdate(adoc("id", "2", "non_tok", "hello world")));
        assertNull(h.validateUpdate(adoc("id", "3", "non_tok", "hello cincinatti")));
        assertNull(h.validateUpdate(commit()));
    }

}
