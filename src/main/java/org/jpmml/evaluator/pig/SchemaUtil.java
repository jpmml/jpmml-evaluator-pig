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
import java.util.List;

import org.apache.pig.data.DataType;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.ModelField;

public class SchemaUtil {

	private SchemaUtil(){
	}

	static
	public Schema.FieldSchema createFieldSchema(ModelField field){
		FieldName name = field.getName();
		org.dmg.pmml.DataType dataType = field.getDataType();

		return new Schema.FieldSchema(name.getValue(), translateDataType(dataType));
	}

	static
	public Schema createTupleSchema(List<? extends ModelField> fields){
		Schema result = new Schema();

		for(ModelField field : fields){
			result.add(createFieldSchema(field));
		}

		return result;
	}

	static
	private byte translateDataType(org.dmg.pmml.DataType dataType){

		switch(dataType){
			case STRING:
				return DataType.CHARARRAY;
			case INTEGER:
				return DataType.INTEGER;
			case FLOAT:
				return DataType.FLOAT;
			case DOUBLE:
				return DataType.DOUBLE;
			case BOOLEAN:
				return DataType.BOOLEAN;
			default:
				return DataType.ERROR;
		}
	}

	static
	public <F extends ModelField> List<Mapping<F>> mapAll(List<? extends F> fields, Schema schema) throws FrontendException {
		List<Mapping<F>> result = new ArrayList<>();

		for(F field : fields){
			FieldName name = field.getName();

			int position = schema.getPosition(name.getValue());
			if(position < 0){
				throw new FrontendException("Input field " + name.getValue() + " does not have a schema mapping");
			}

			result.add(new Mapping<>(field, position));
		}

		return result;
	}
}