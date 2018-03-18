iris_data = LOAD 'src/test/pig/Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:double, Petal_Width:double);

DEFINE iris_dt org.jpmml.evaluator.pig.ComplexPMMLFunc('src/test/pig/DecisionTreeIris.pmml');

iris_classification = FOREACH iris_data GENERATE iris_dt(*);

DUMP iris_classification;