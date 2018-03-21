iris_data = LOAD 'src/test/pig/Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:chararray, Petal_Width:chararray);

DEFINE iris_pmml org.jpmml.evaluator.pig.PMMLFunc('src/test/pig/DecisionTreeIris.pmml');

iris_classification = FOREACH iris_data GENERATE iris_pmml(*);

DUMP iris_classification;
