The whole follow
1.compile solr
2.dist solr
3.add extend jar file include Lire.jar and so on in extend directory "/extend/lib". 
  note that lire.jar must be compatible with lucene in solr, since when use lire maybe refer lucene interface and data structure
4.compile extend functionality code
5.make jar file for the extend code
6.dist extend solr (directly use this step can jump 5)

command follow 
1.In Solr4.0Extends project root directory, issue "ant" in command prompt
2.In Solr4.0Extends project root directory, issue "dist" in command prompt