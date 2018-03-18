iris_data = LOAD 'src/test/pig/Iris.csv' USING PigStorage(',') AS (Sepal_Length:double, Sepal_Width:double, Petal_Length:double, Petal_Width:double);

DEFINE iris_dt org.jpmml.evaluator.pig.SimplePMMLFunc('src/test/pig/DecisionTreeIris.pmml');

iris_species = FOREACH iris_data GENERATE iris_dt(*);

DUMP iris_species;
