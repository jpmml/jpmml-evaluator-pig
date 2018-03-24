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
import java.io.IOException;
import java.util.List;

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

public class ArchiveBuilderFunc extends EvalFunc<String> {

	@Override
	public String exec(Tuple tuple) throws IOException {

		if(tuple == null || tuple.size() != 3){
			return null;
		}

		// XXX
		List<String> values = (List)tuple.getAll();

		String className = values.get(0);

		File pmmlFile = new File(values.get(1));
		File udfJarFile = new File(values.get(2));

		try {
			CodeModelUtil.build(className, pmmlFile, udfJarFile);
		} catch(Exception e){
			throw new ExecException(e);
		}

		return udfJarFile.getAbsolutePath();
	}
}