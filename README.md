# Match Query Parser

Tightly control how Solr queries are parsed and executed by parsing the query using a query analyzer, not whitespace tokenized. (happens to also be a fix for multiterm synonyms).

as an edismax boost:

```
q=seabiscuit likes to fish&bq={!match analyze_as=text_synonym search_with=phrase qf=body v=$q}
```

Match qp goes through these three steps

1. Analyze the query string using `text_synonym` field type, perhaps resulting in [sea biscuit] [likes] [to] [fish]
2. Treat the resulting tokens as phrase queries `"sea biscuit" OR "likes" OR "to" OR "fish"`
3. Search the `body` field 

The ability control analysis and the type of query used let's you apply an extreme level of control to search. For example, if you repeat the above example with a shingle analyzer, you can recreate the behavior of pf2 in edismax:

1. Analyze the query string using `text_shingle` field type, perhaps resulting in [sea biscuit] [biscuit likes] [biscuit likes] ... 
2. Treat the resulting tokens as phrase queries `"sea biscuit" OR "biscuit likes" OR "biscuit likes" ...`
3. Search the `body` field

Read more in [this tutorial](TUTORIAL.md).

# Parameters

### qf

A single field to be searched.

### analyze_as 

Use this field type for analysis. The field type's query-time analyzer is used to analyze the query string. When using match qp, I often create field types for the sole purpose of having different query-time analyzers at my disposal.

If omitted, uses the query analysis of qf.

### search_with

 Either `term` (default) or `phrase`.
 
 - `term` the tokens output from analysis from step (1) above are turned into term queries
 - `phrase` the tokens output  from analysis from step (1) are whitespace tokenized, and turned into phrase queries   
 
 An important note about position overlaps. In the above example, we pretended that seabiscuit was transformed into just `[sea biscuit]` when in reality, both tokens `[seabiscuit]` and `[sea biscuit]` would be omitted in the same position. In this case, tokens in the same position are wrapped in a DisjunctionMaximum (dismax) query. So the actual query would be, using | to show the dismax operation
  
 `("sea biscuit" | seabiscuit) OR likes OR to OR fish` 

### mm

Min-should-match expression used to specify the mm of the outer boolean query.

### pslop

Phrase slop to use for phrase query type.

# Acknowledgements

 - This is somewhat inspired by Elasticsearch's [match query](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html))
 - Sponsored by [OpenSource Connections](http://opensourceconnections.com)

