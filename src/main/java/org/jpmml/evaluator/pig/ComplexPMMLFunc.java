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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.pig.PigException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ResultField;

public class ComplexPMMLFunc extends PMMLFunc<Tuple> {

	private TupleFactory tupleFactory = TupleFactory.getInstance();

	private List<Mapping<ResultField>> resultMappings = null;


	public ComplexPMMLFunc(String path) throws Exception {
		this(EvaluatorUtil.createEvaluator(path));
	}

	public ComplexPMMLFunc(Evaluator evaluator){
		super(evaluator);
	}

	@Override
	public Tuple encodeOutput(Map<FieldName, ?> result) throws PigException {
		TupleFactory tupleFactory = getTupleFactory();
		List<Mapping<ResultField>> resultMappings = ensureResultMappings();

		Object[] pigValues = new Object[resultMappings.size()];

		for(Mapping<ResultField> resultMapping : resultMappings){
			ResultField resultField = resultMapping.getField();
			int position = resultMapping.getPosition();

			Object pmmlValue = result.get(resultField.getName());

			pigValues[position] = org.jpmml.evaluator.EvaluatorUtil.decode(pmmlValue);
		}

		return tupleFactory.newTupleNoCopy(Arrays.asList(pigValues));
	}

	@Override
	public Schema outputSchema(Schema schema){
		String name = (this.getClass()).getName();

		try {
			return new Schema(new Schema.FieldSchema(getSchemaName(name.toLowerCase(), schema), tupleSchema(), DataType.TUPLE));
		} catch(FrontendException fe){
			return super.outputSchema(schema);
		}
	}

	public List<? extends ResultField> getResultFields(){
		Evaluator evaluator = getEvaluator();

		List<ResultField> result = new ArrayList<>();
		result.addAll(evaluator.getTargetFields());
		result.addAll(evaluator.getOutputFields());

		return result;
	}

	private Schema tupleSchema(){
		List<? extends ResultField> resultFields = getResultFields();

		return SchemaUtil.createTupleSchema(resultFields);
	}

	private List<Mapping<ResultField>> ensureResultMappings() throws FrontendException {

		if(this.resultMappings == null){
			this.resultMappings = SchemaUtil.mapAll(getResultFields(), tupleSchema());
		}

		return this.resultMappings;
	}

	private TupleFactory getTupleFactory(){
		return this.tupleFactory;
	}
}