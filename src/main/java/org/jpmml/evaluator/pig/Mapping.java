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

import org.jpmml.evaluator.ModelField;

class Mapping<F extends ModelField> {

	private F field = null;

	private int position = -1;


	Mapping(F field, int position){
		setField(field);
		setPosition(position);
	}

	public F getField(){
		return this.field;
	}

	private void setField(F field){
		this.field = field;
	}

	public int getPosition(){
		return this.position;
	}

	private void setPosition(int position){
		this.position = position;
	}
}