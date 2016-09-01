package com.o19s.solr.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by doug on 8/10/16.
 */
public class MatchQParser extends QParser {

    interface QueryFactory {

        public Query createQuery(Term currTerm) throws IOException;
    }

    private QueryFactory createQueryFactory(String qf, String clauseType, String ptok, int pslop, String tokenizer) {
        Tokenizer phraseTok = new WhitespaceTokenizer();

        if (clauseType == "term") {
            return (Term clauseTerm) -> {
                return new TermQuery(clauseTerm);
            };
        } else {
            return (Term clauseTerm) -> {
                StringReader stringReader = new StringReader(clauseTerm.text());
                CharTermAttribute term = phraseTok.addAttribute(CharTermAttribute.class);
                phraseTok.setReader(stringReader);
                phraseTok.reset();
                PhraseQuery.Builder pqb = new PhraseQuery.Builder();
                while (phraseTok.incrementToken()) {
                    String token = term.toString();
                    pqb.add(new Term(qf, token));
                }
                pqb.setSlop(pslop);
                phraseTok.close();
                return pqb.build();

            };

        }
    }

    private String getBestParam(SolrParams globalParams, SolrParams localParams, String parameter, String defVal) {
        SolrParams preferredParams = localParams;
        if (preferredParams == null) {
            preferredParams = params;
        }
        return preferredParams.get(parameter, defVal);
    }

    private String getQueryText(String qStr, SolrParams params, SolrParams localParams) {
        if (qStr == null) {
            return localParams.get(QueryParsing.V);
        }
        return qStr;
    }

    public MatchQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    public Query parse() throws SyntaxError {
        String qf = getBestParam(params, localParams, "qf", null);

        // Field type to use for query-time analysis, for example
        // you may just want to create a field type for the sole purpose
        // of using its analyzer to query this field
        // - Defaults to qf
        String ft = getBestParam(params, localParams, "ft", qf);

        // Clause type this query will generate after analysis
        // - -- term (default) -- transforms analyzed query tokens into term queries
        // - -- phrase         -- each query token is treated as if a phrase,
        //                        for example if a bigram is generated, and the
        //                        phrase delimeter is whitespace, then
        String ct = getBestParam(params, localParams, "ct", "term");

        // Phrase tok (ptok) will control how phrase queries are generated from
        // analysis
        //
        //  standard (standardtokenizerfactory)
        //  whitespace  (whitespacetokenizer)
        String ptok = getBestParam(params, localParams, "ptok", "standard");

        int pslop = Integer.parseInt(getBestParam(params, localParams, "pslop", "0"));

        String minShouldMatch = getBestParam(params, localParams, "mm", "0");

        String queryText = getQueryText(qstr, params, localParams);

        QueryFactory baseQueryFactory = createQueryFactory(qf, ct, ptok, pslop, ptok);

        FieldType fieldType = req.getSchema().getFieldTypeByName(ft);
        Analyzer ftQAnalyzer = fieldType.getQueryAnalyzer();
        TokenStream tokenStream = null;
        try {
            tokenStream = ftQAnalyzer.tokenStream(ft, new StringReader(queryText));

            BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();

            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute posn = tokenStream.addAttribute(PositionIncrementAttribute.class);
            tokenStream.reset();
            int currPos = -1;
            ArrayList<Query> currPosnClauses = null;
            while (tokenStream.incrementToken()) {
                if (posn.getPositionIncrement() > 0) {
                    if (currPosnClauses != null) {
                        BooleanClause c = new BooleanClause(new DisjunctionMaxQuery(currPosnClauses, 0.0f), BooleanClause.Occur.SHOULD);
                        bqBuilder.add(c);
                    }
                    currPosnClauses = new ArrayList<Query>();

                }
                Term clauseTerm = new Term(qf, term.toString());
                assert currPosnClauses != null;
                currPosnClauses.add(baseQueryFactory.createQuery(clauseTerm));
            }
            if (currPosnClauses != null) {
                BooleanClause c = new BooleanClause(new DisjunctionMaxQuery(currPosnClauses, 0.0f), BooleanClause.Occur.SHOULD);
                bqBuilder.add(c);
            }
            tokenStream.end();

            SolrPluginUtils.setMinShouldMatch(bqBuilder, minShouldMatch);


            return bqBuilder.build();
        } catch (IOException e) {
            return null;
        }
        finally {
            IOUtils.closeWhileHandlingException(tokenStream);
        }

    }
}
