/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.api.functions.windowing;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.operators.translation.WrappingFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.util.Collections;

@Internal
public class AggregateApplyWindowFunction<K, W extends Window, T, ACC, R>
	extends WrappingFunction<WindowFunction<R, R, K, W>>
	implements WindowFunction<T, R, K, W> {

	private static final long serialVersionUID = 1L;

	private final AggregateFunction<T, ACC, R> aggFunction;

	public AggregateApplyWindowFunction(AggregateFunction<T, ACC, R> aggFunction, WindowFunction<R, R, K, W> windowFunction) {
		super(windowFunction);
		this.aggFunction = aggFunction;
	}

	@Override
	public void apply(K key, W window, Iterable<T> values, Collector<R> out) throws Exception {
		final ACC acc = aggFunction.createAccumulator();

		for (T val : values) {
			aggFunction.add(val, acc);
		}

		wrappedFunction.apply(key, window, Collections.singletonList(aggFunction.getResult(acc)), out);
	}
}
