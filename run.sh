#bin/nutch recrawl urls -dir nutchdb -solr http://localhost:8983/solr/ -depth 2 -topN 500 -loop 10 -index
bin/nutch recrawl urls -dir nutchdb -solr http://localhost:8983/solr/ -depth 1 -topN 500 -loop 1 -index
