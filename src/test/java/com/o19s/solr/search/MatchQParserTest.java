package com.o19s.solr.search;

import org.apache.solr.SolrTestCaseJ4;
import org.junit.After;
import org.junit.Before;
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

    @After
    public void clearCore() throws SAXException {
        assertNull(h.validateUpdate(delQ("*:*")));
        assertNull(h.validateUpdate(commit()));

        assertU(optimize());
    }

    @Test
    public void testUseQfToAnalyze() throws SAXException {
        assertNull(h.validateUpdate(adoc("id", "1", "text", "HELLO funny WORLD")));
        assertNull(h.validateUpdate(adoc("id", "2", "text", "hello world and cincinatti")));
        assertNull(h.validateUpdate(adoc("id", "3", "text", "hello cincinatti bananas")));
        assertNull(h.validateUpdate(commit()));
        assertU(optimize());


        assertQ("analyze with other field",
                req("defType", "match",
                        "q", "hello world",
                        "qt", "select",
                        "qf", "text"),
                "//*[@numFound='3']"
        );

        assertQ("analyze with other field",
                req("defType", "match",
                        "q", "hello world",
                        "qt", "select",
                        "qf", "text",
                        "mm", "100%"),
                "//*[@numFound='2']"
        );


    }

    @Test
    public void testBigramPhraseSearch() throws SAXException {
        assertNull(h.validateUpdate(adoc("id", "1", "text", "HELLO WORLD")));
        assertNull(h.validateUpdate(adoc("id", "2", "text", "hello world and cincinatti")));
        assertNull(h.validateUpdate(adoc("id", "3", "text", "hello cincinatti bananas")));
        assertNull(h.validateUpdate(commit()));
        assertU(optimize());


        // formulate a query that will by analyzed by the shingle field type and then turned into a phrase query
        assertQ("analyze with shingle field, search phrases",
                req("defType", "match",
                        "q", "hello cincinatti bengals",
                        "qt", "select",
                        "qf", "text",
                        "search_with", "phrase",
                        "analyze_as", "shingled"),
                "//*[@numFound='1']", "//result/doc[1]/str[@name='id'][.='3']"
        );
    }

    @Test
    public void testMultitermSynonyms() throws SAXException {
        assertNull(h.validateUpdate(adoc("id", "1", "text", "seabiscuit")));
        assertNull(h.validateUpdate(adoc("id", "2", "text", "sea biscuit the lonely horse")));
        assertNull(h.validateUpdate(commit()));
        assertU(optimize());

        // formulate a query that will by analyzed by the shingle field type and then turned into a phrase query
        assertQ("analyze with syn field, search phrases, defeat multi term synonyms",
                req("defType", "match",
                        "q", "sea biscuit",
                        "qt", "select",
                        "qf", "text",
                        "search_with", "phrase",
                        "analyze_as", "synonymized"),
                "//*[@numFound='2']");
    }
}
