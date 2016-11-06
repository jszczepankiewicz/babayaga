# BabaYaga
[![Build Status](https://travis-ci.org/jszczepankiewicz/babayaga.svg?branch=master)](https://travis-ci.org/jszczepankiewicz/babayaga)

PoC to create schemaless document storage in Kotlin on top of RDBMS heavily nspired by:

1. Uber schemaless: https://eng.uber.com/schemaless-part-one/
2. FriendFeed: https://backchannel.org/blog/friendfeed-schemaless-mysql

#### Goals
1. Data migration without altering RDBMS schema
2. Create & Drop Indexes on the flight immediately regardless of collection size
3. Background db clean up and indexes maintenance

#### Development decisions
##### Testing
1. Chosen JUnit runner with Java Style testing for low level tests
2. For higher level tests Aspen chosen, rejected Spek as 1.0 (Spek) does not support Spring JUnit runner (https://github.com/JetBrains/spek/issues/50). 

#### Architecture decisions
##### (De)serialization framework
For (de)serialization http://msgpack.org/ was chosen. One of nice bug(feature) is lossless compression of integer values to lower representation if fits into lower range (i.e. long might be stored in byte). As it is not planned to have interchange with outside world it is possible to replace with some even more space friendly solution. Further compression on binary representation is planned (LZ4 probably).

#### TODO
1. Add support for LZ4 compression (i.e. lz4-java) for bodies
2. Add support for metrics (i.e. https://github.com/ryantenney/metrics-spring)

#### Usefull links
https://github.com/jamesgolick/friendly#readme
https://github.com/eklitzke/schemaless

#### Planned versions
A. one version / tuple per id in entities table, same entry updated multiple times. Possible race conditions 
A-bis. transform to use ActiveRecord but avoid beginner DDD traps (factories?) http://mortslikeus.blogspot.com/2009/01/active-record-and-ddd.html
B. multiple versions / tuples per id in entities table. Each item maybe filled only once. Minimize (eliminate?) race conditions
C. One entry may be scattered per mutliple entries. 
D. Transformed from library to full fledged distributed nodes to allow horizontal scalability out-of-the-box

