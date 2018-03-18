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

import java.util.List;
import java.util.Map;

import org.apache.pig.PigException;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ResultField;
import org.jpmml.evaluator.TargetField;

public class SimplePMMLFunc extends PMMLFunc<Object> {

	public SimplePMMLFunc(String path) throws Exception {
		this(EvaluatorUtil.createEvaluator(path));
	}

	public SimplePMMLFunc(Evaluator evaluator){
		super(evaluator);
	}

	@Override
	public Object encodeOutput(Map<FieldName, ?> result) throws PigException {
		ResultField resultField = getResultField();

		Object pmmlValue = result.get(resultField.getName());

		return org.jpmml.evaluator.EvaluatorUtil.decode(pmmlValue);
	}

	@Override
	public Schema outputSchema(Schema schema){

		try {
			return new Schema(fieldSchema());
		} catch(FrontendException fe){
			return super.outputSchema(schema);
		}
	}

	public ResultField getResultField() throws FrontendException {
		Evaluator evaluator = getEvaluator();

		List<TargetField> targetFields = evaluator.getTargetFields();
		if(targetFields.size() != 1){
			throw new FrontendException();
		}

		return targetFields.get(0);
	}

	private Schema.FieldSchema fieldSchema() throws FrontendException {
		ResultField resultField = getResultField();

		return SchemaUtil.createFieldSchema(resultField);
	}
}