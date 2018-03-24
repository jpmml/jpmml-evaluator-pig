/*
 * Copyright (c) 2018 Villu Ruusmann
 *
 * This file is part of JPMML-Evaluator
 *
 * JPMML-Evaluator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Evaluator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Evaluator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.evaluator.pig;

import org.apache.pig.pigunit.PigTest;
import org.junit.Test;

public class EvaluatorFuncTest {

	@Test
	public void evaluate() throws Exception {
		PigTest pigTest = new PigTest("src/test/pig/EvaluatorFunc.pig");

		String[] iris_classification = {
			"((setosa,2))",
			"((versicolor,6))",
			EvaluatorFuncTest.NULL,
			"((virginica,7))",
			EvaluatorFuncTest.NULL
		};

		pigTest.assertOutput("Iris_classification", iris_classification);
	}

	public static final String NULL = "()";
}