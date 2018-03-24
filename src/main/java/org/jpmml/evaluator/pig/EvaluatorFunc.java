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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluationException;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.InvalidFeatureException;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.ResultField;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.UnsupportedFeatureException;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.visitors.LocatorTransformer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EvaluatorFunc extends EvalFunc<Tuple> {

	private Resource resource = null;

	private Evaluator evaluator = null;

	private List<Mapping<InputField>> inputMappings = null;

	private List<Mapping<ResultField>> outputMappings = null;


	public EvaluatorFunc(String path){
		this(new FileResource(new File(path)));
	}

	public EvaluatorFunc(Resource resource){
		setResource(resource);
	}

	@Override
	public Tuple exec(Tuple tuple) throws PigException {
		Evaluator evaluator = ensureEvaluator();

		if(tuple == null || tuple.size() == 0){
			return null;
		}

		String message = null;

		try {
			message = "Failed to decode arguments";

			Map<FieldName, FieldValue> arguments;

			try {
				arguments = decodeInput(tuple);
			} catch(IllegalArgumentException iae){
				super.log.warn(message, iae);

				return null;
			}

			message = "Failed to evaluate";

			Map<FieldName, ?> result = evaluator.evaluate(arguments);

			message = "Failed to encode results";

			return encodeOutput(result);
		} catch(EvaluationException ee){
			super.log.warn(message, ee);

			return null;
		} catch(InvalidFeatureException | UnsupportedFeatureException fe){
			super.log.error(message, fe);

			throw new ExecException(fe);
		}
	}

	@Override
	public List<String> getShipFiles(){
		Resource resource = getResource();

		return resource.getShipFiles();
	}

	@Override
	public Schema outputSchema(Schema schema){
		String name = (this.getClass()).getName();

		try {
			List<ResultField> resultFields = getResultFields();

			return new Schema(new Schema.FieldSchema(getSchemaName(name.toLowerCase(), schema), SchemaUtil.createTupleSchema(resultFields), DataType.TUPLE));
		} catch(FrontendException fe){
			return super.outputSchema(schema);
		}
	}

	private Map<FieldName, FieldValue> decodeInput(Tuple tuple) throws PigException {
		List<Mapping<InputField>> inputMappings = ensureInputMappings();

		Map<FieldName, FieldValue> result = new HashMap<>();

		for(Mapping<InputField> inputMapping : inputMappings){
			InputField inputField = inputMapping.getField();
			int position = inputMapping.getPosition();

			Object pigValue = tuple.get(position);

			FieldName name = inputField.getName();
			FieldValue value = inputField.prepare(pigValue);

			result.put(name, value);
		}

		return result;
	}

	private Tuple encodeOutput(Map<FieldName, ?> result) throws PigException {
		List<Mapping<ResultField>> outputMappings = ensureOutputMappings();

		Tuple tuple = EvaluatorFunc.tupleFactory.newTuple(outputMappings.size());

		for(Mapping<ResultField> outputMapping : outputMappings){
			ResultField resultField = outputMapping.getField();
			int position = outputMapping.getPosition();

			Object pmmlValue = result.get(resultField.getName());

			if(resultField instanceof TargetField){
				pmmlValue = EvaluatorUtil.decode(pmmlValue);
			}

			tuple.set(position, pmmlValue);
		}

		return tuple;
	}

	private List<InputField> getInputFields() throws FrontendException {
		Evaluator evaluator = ensureEvaluator();

		return evaluator.getInputFields();
	}

	private List<ResultField> getResultFields() throws FrontendException {
		Evaluator evaluator = ensureEvaluator();

		List<ResultField> result = new ArrayList<>();
		result.addAll(evaluator.getTargetFields());
		result.addAll(evaluator.getOutputFields());

		return result;
	}

	private Evaluator ensureEvaluator() throws FrontendException {

		if(this.evaluator == null){
			this.evaluator = createEvaluator();
		}

		return this.evaluator;
	}

	private Evaluator createEvaluator() throws FrontendException {
		Resource resource = getResource();

		try(InputStream is = resource.getInputStream()){
			return createEvaluator(is);
		} catch(Exception e){
			throw new FrontendException("Failed to create model evaluator", e);
		}
	}

	private List<Mapping<InputField>> ensureInputMappings() throws FrontendException {

		if(this.inputMappings == null){
			List<InputField> inputFields = getInputFields();

			this.inputMappings = SchemaUtil.mapAll(inputFields, getInputSchema());
		}

		return this.inputMappings;
	}

	private List<Mapping<ResultField>> ensureOutputMappings() throws FrontendException {

		if(this.outputMappings == null){
			List<ResultField> resultFields = getResultFields();

			this.outputMappings = SchemaUtil.mapAll(resultFields, SchemaUtil.createTupleSchema(resultFields));
		}

		return this.outputMappings;
	}

	public Resource getResource(){
		return this.resource;
	}

	private void setResource(Resource resource){
		this.resource = resource;
	}

	static
	private Evaluator createEvaluator(InputStream is) throws SAXException, JAXBException {
		Source source = ImportFilter.apply(new InputSource(is));

		PMML pmml = JAXBUtil.unmarshalPMML(source);

		// If the SAX Locator information is available, then transform it to java.io.Serializable representation
		LocatorTransformer locatorTransformer = new LocatorTransformer();
		locatorTransformer.applyTo(pmml);

		ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();

		Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);

		// Perform self-testing
		evaluator.verify();

		return evaluator;
	}

	private static final TupleFactory tupleFactory = TupleFactory.getInstance();
}