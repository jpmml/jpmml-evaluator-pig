Iris = LOAD 'src/test/pig/Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:chararray, Petal_Width:chararray);

DEFINE DecisionTreeIris org.jpmml.evaluator.pig.EvaluatorFunc('src/test/pig/DecisionTreeIris.pmml');

Iris_classification = FOREACH Iris GENERATE DecisionTreeIris(*);

DUMP Iris_classification;
