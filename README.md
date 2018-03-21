JPMML-Evaluator-Pig
===================

PMML evaluator library for the Apache Pig platform (http://pig.apache.org/).

# Features #

* Full support for PMML specification versions 3.0 through 4.3. The evaluation is handled by the [JPMML-Evaluator](https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* Apache Pig version 0.14.0 or newer.

# Installation #

Enter the project root directory and build using [Apache Maven](http://maven.apache.org/):
```
mvn clean install
```

The build produces a library JAR file `jpmml-evaluator-pig-1.0-SNAPSHOT.jar` and a runtime uber-JAR file (the library JAR file plus all transitive dependencies) `jpmml-evaluator-pig-runtime-1.0-SNAPSHOT.jar`.

# Usage #

Adding the runtime uber-JAR file to Apache Pig classpath:
```
REGISTER /path/to/jpmml-evaluator-pig-runtime-1.0-SNAPSHOT.jar;
```

Loading the Iris dataset:
```
iris_data = LOAD 'Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:double, Petal_Width:double);

DESCRIBE iris_data;
DUMP iris_data;
```

Scoring the Iris dataset using the `org.jpmml.evaluator.pig.PMMLFunc` user defined function (UDF) class:
```
DEFINE iris_pmml org.jpmml.evaluator.pig.PMMLFunc('DecisionTreeIris.pmml');

iris_classification = FOREACH iris_data GENERATE iris_pmml(*);

DESCRIBE iris_classification;
DUMP iris_classification;
```

# License #

JPMML-Evaluator-Pig is licensed under the [GNU Affero General Public License (AGPL) version 3.0](http://www.gnu.org/licenses/agpl-3.0.html). Other licenses are available on request.

# Additional information #

Please contact [info@openscoring.io](mailto:info@openscoring.io)
