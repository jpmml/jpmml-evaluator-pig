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

### Configuration ###

Add the runtime uber-JAR file to Apache Pig classpath:
```
REGISTER jpmml-evaluator-pig-runtime-1.0-SNAPSHOT.jar;
```

### Defining PMML functions ###

Define the PMML function by instantiating the `org.jpmml.evaluator.pig.EvaluatorFunc` PMML UDF class. The public constructor of this class takes exactly one string argument, which is the path to the PMML document in local filesystem:
```
DEFINE DecisionTreeIris org.jpmml.evaluator.pig.EvaluatorFunc('DecisionTreeIris.pmml');
```

Alternatively, create a subclass of the PMML UDF class:
```Java
package com.mycompany;

import org.jpmml.evaluator.pig.ArchiveResource;
import org.jpmml.evaluator.pig.EvaluatorFunc;

public class DecisionTreeIris extends EvaluatorFunc {

	public DecisionTreeIris(){
		super(new ArchiveResource("/DecisionTreeIris.pmml"){ /* Anonymous inner class */ });
	}
}
```

Package this subclass together with the accompanying PMML document (and other metadata such as the Service Loader configuration file) into a so-called model JAR file:
```
$ unzip -l DecisionTreeIris.jar
Archive:  DecisionTreeIris.jar
  Length      Date    Time    Name
---------  ---------- -----   ----
       25  03-24-2018 14:47   META-INF/MANIFEST.MF
     4480  03-24-2018 14:47   DecisionTreeIris.pmml
       30  03-24-2018 14:47   META-INF/services/org.jpmml.evaluator.pig.EvaluatorFunc
      306  03-24-2018 14:47   com/mycompany/DecisionTreeIris.java
      370  03-24-2018 14:47   com/mycompany/DecisionTreeIris$1.class
      425  03-24-2018 14:47   com/mycompany/DecisionTreeIris.class
---------                     -------
     5636                     6 files
```

The subclassing and packaging process can be automated using the `org.jpmml.evaluator.pig.ArchiveBuilderFunc` UDF class. This UDF takes a tuple of three string values (the fully qualified name of the subclass, tha paths to the PMML document and the model JAR file in local filesystem) as input, and returns a string value (the path to the model JAR file in local filesystem) as output:
```
Udf = LOAD 'Udf.csv' USING PigStorage(',') AS (Class_Name:chararray, PMML_File:chararray, Model_Jar_File:chararray);

Udf_model_jar_file = FOREACH Udf GENERATE org.jpmml.evaluator.pig.ArchiveBuilderFunc(*);

DUMP Udf_model_jar_file;
```

Add the model JAR file to Apache Pig classpath, and define the PMML function by instantiating the `com.mycompany.DecisionTreeIris` UDF class:
```
ADD DecisionTreeIris.jar;

DEFINE DecisionTreeIris com.mycompany.DecisionTreeIris;
```

### Applying PMML functions to data ###

All PMML UDF classes take a tuple as input, and return a tuple as output.

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
