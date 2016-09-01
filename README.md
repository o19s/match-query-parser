(IN PROGRESS, NOT READY FOR PRIME TIME)

# Match Query Parser

Tightly control how Solr queries are parsed and executed.


## 1. Control query string parsing by specifying any analyzer

Use *any* analyzer to use to parse your query string -- even an analyzer unassociated with the field you're searching. Instead of the default whitespace delimited methods, you take very specific control over what terms are searched for. 

Let's take an example. You decide you want to change how a field `body` is searched. This field uses a boring solr field type like

```
    <fieldType name="text_general" class="solr.TextField" positionIncrementGap="100" multiValued="true">
      <analyzer>
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>
      <analyzer type="query">
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>
    </fieldType>
```

But you decide that in addition to searching the boring way (tokenization and lowercasing) you also want to search using synonyms. So you create a dummy field type 

```
       <fieldType name="synonymized" class="solr.TextField">
            <analyzer type="query">
                <tokenizer class="solr.WhitespaceTokenizerFactory"/>
                <filter class="solr.LowerCaseFilterFactory"/>
                <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt"
                        format="solr" ignoreCase="false" expand="true"
                        tokenizerFactory="solr.WhitespaceTokenizerFactory"/>
            </analyzer>
        </fieldType>
```


With match query parser, you can search the same field both with multiple forms of query-time analysis. Here we use match qp as a boost query on top of a base edismax query, boosting on the occurence of synonyms.

`q=sea biscuit&defType=edismax&qf=body&bq={!match qf=body analyze_as=synonymized v=$q}`

This query will search for sea biscuit in field body, but will also search for all terms generated by the query analyzer of `text_general_syn`. This includes `seabiscuit`, `sea biscuit the lonely horse`.

## 2. Control how terms resulting from query-time analysis result in Lucene queries

By default, match qp will turn the resulting tokens into term queries that are OR'd together. For example, `sea OR biscuit OR the OR lonely OR horse`. 

Of course, this isn't typically what you want with synonyms. You likely want to search for the phrase "sea biscuit the lonely horse" as a full phrase. So match qp let's you control how to treat the resulting tokens. We just need to generate "sea biscuit the lonely horse" as a single term from analysis.

One way to do this would be to use a slighly different `synonyms.txt` file.

```
sea biscuit => seabiscuit, sea_biscuit_the_lonely_horse
``` 

and add a step to synonymized to turn underscores into spaces:

```
<filter class="solr.PatternReplaceFilterFactory" pattern="(_)" replacement=" "
        replace="all"/>
```


Now the result of `synonymized` is the token `[sea biscuit the lonely horse]`. The query above would search for a term `[sea biscuit the lonely horse]`. But with "search_as=phrase" the resulting tokens will be themselves whitespace tokenized and turned into phrase queries:

``q=sea biscuit&defType=edismax&qf=body&bq={!match qf=body analyzer=text_general_syn search_with=phrase v=$q}`

This will output a query for 

`seabiscuit` OR `"sea biscuit the lovely horse"`

An optional `pslop` parameter is available to control the slop of this query.

(yes this is somewhat inspired by Elasticsearch's [match query](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html))
