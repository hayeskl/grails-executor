/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.executor

import grails.persistence.support.PersistenceContextInterceptor

import java.util.concurrent.Callable

/**
 * Wraps the execution of a Callable in a persistence context, via the persistenceInterceptor.
 */
class PersistenceContextCallableWrapper<T> extends PersistenceContextWrapper implements Callable<T> {

	private final Callable<T> callable

	PersistenceContextCallableWrapper(PersistenceContextInterceptor persistenceInterceptor, Callable<T> callable) {
		super(persistenceInterceptor)
		this.callable = callable
	}

	T call() {
        wrap { callable.call() }
	}

}