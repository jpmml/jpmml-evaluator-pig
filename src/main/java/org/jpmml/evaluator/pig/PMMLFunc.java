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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;

abstract
public class PMMLFunc<V> extends EvalFunc<V> {

	private File file = null;

	private Evaluator evaluator = null;

	private List<Mapping<InputField>> argumentMappings = null;


	public PMMLFunc(String path) throws FrontendException {
		File file = new File(path);

		if(!file.exists()){
			throw new FrontendException("Local PMML file " + file.getAbsolutePath() + " does not exist");
		}

		setFile(file);
	}

	abstract
	public V encodeOutput(Map<FieldName, ?> result) throws PigException;

	@Override
	public V exec(Tuple tuple) throws PigException {
		Evaluator evaluator = ensureEvaluator();

		Map<FieldName, FieldValue> arguments = decodeInput(tuple);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		return encodeOutput(result);
	}

	public Map<FieldName, FieldValue> decodeInput(Tuple tuple) throws PigException {
		List<Mapping<InputField>> argumentMappings = ensureArgumentMappings();

		Map<FieldName, FieldValue> result = new HashMap<>();

		for(Mapping<InputField> argumentMapping : argumentMappings){
			InputField inputField = argumentMapping.getField();
			int position = argumentMapping.getPosition();

			Object pigValue = tuple.get(position);

			FieldName name = inputField.getName();
			FieldValue value = inputField.prepare(pigValue);

			result.put(name, value);
		}

		return result;
	}

	@Override
	public List<String> getShipFiles(){
		File file = getFile();

		return Collections.singletonList(file.getAbsolutePath());
	}

	public List<InputField> getInputFields() throws FrontendException {
		Evaluator evaluator = ensureEvaluator();

		return evaluator.getInputFields();
	}

	public Evaluator ensureEvaluator() throws FrontendException {

		if(this.evaluator == null){
			this.evaluator = createEvaluator();
		}

		return this.evaluator;
	}

	private Evaluator createEvaluator() throws FrontendException {
		File file = getFile();

		if(file.exists()){
			super.log.info("Loading local PMML file " + file.getAbsolutePath());
		} else

		{
			file = new File(file.getName());

			super.log.info("Loading distributed cache PMML file " + file.getAbsolutePath());
		}

		try(InputStream is = new FileInputStream(file)){
			return EvaluatorUtil.createEvaluator(is);
		} catch(Exception e){
			throw new FrontendException("Failed to load PMML file", e);
		}
	}

	private List<Mapping<InputField>> ensureArgumentMappings() throws FrontendException {

		if(this.argumentMappings == null){
			this.argumentMappings = SchemaUtil.mapAll(getInputFields(), getInputSchema());
		}

		return this.argumentMappings;
	}

	public File getFile(){
		return this.file;
	}

	private void setFile(File file){
		this.file = file;
	}
}