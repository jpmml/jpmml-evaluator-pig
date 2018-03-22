JPMML-Evaluator-Pig
===================

PMML evaluator library for the Apache Pig platform (http://pig.apache.org/).

# Features #

* Full support for PMML specification versions 3.0 through 4.3. The evaluation is handled by the [JPMML-Evaluator](https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* Apache Hadoop 2.7.0 or newer.
* Apache Pig version 0.14.0 or newer.

# Installation #

Enter the project root directory and build using [Apache Maven](http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:

* `target/jpmml-evaluator-pig-1.0-SNAPSHOT.jar` - the library JAR file.
* `target/jpmml-evaluator-pig-runtime-1.0-SNAPSHOT.jar` - the runtime uber-JAR file (the library JAR file plus all its transitive dependencies).

# Usage #

Add the runtime uber-JAR file to Apache Pig classpath:
```
REGISTER jpmml-evaluator-pig-runtime-1.0-SNAPSHOT.jar;
```

Define a function by instantiating the `org.jpmml.evaluator.pig.PMMLFunc` user defined function (UDF) class. The public constructor takes exactly one string argument, which is the path to the PMML document in local filesystem:
```
DEFINE DecisionTreeIris org.jpmml.evaluator.pig.PMMLFunc('DecisionTreeIris.pmml');
```

Load and score the Iris dataset:
```
Iris = LOAD 'Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:double, Petal_Width:double);

DESCRIBE Iris;
DUMP Iris;

Iris_classification = FOREACH Iris GENERATE DecisionTreeIris(*);

DESCRIBE Iris_classification;
DUMP Iris_classification;
```

# License #

JPMML-Evaluator-Pig is licensed under the [GNU Affero General Public License (AGPL) version 3.0](http://www.gnu.org/licenses/agpl-3.0.html). Other licenses are available on request.

# Additional information #

Please contact [info@openscoring.io](mailto:info@openscoring.io)
