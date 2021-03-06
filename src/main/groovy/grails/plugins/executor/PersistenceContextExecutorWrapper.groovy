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
import groovy.util.logging.Slf4j
import org.springframework.util.Assert

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Wraps an ExecutorService, overriding the submitting methods to have the work done in a
 * persistence context (via the persistenceInterceptor) and adds new methods that make it possible
 * to still do work without opening a persistence context.
 */
@Slf4j
class PersistenceContextExecutorWrapper {

	// Autowired
	@Delegate ExecutorService executor
	PersistenceContextInterceptor persistenceInterceptor

	void execute(Runnable command) {
		executor.execute(inPersistence(command))
	}

	void executeWithoutPersistence(Runnable command) {
		executor.execute(command)
	}

	def <T> Future<T> submit(Callable<T> task) {
		executor.submit(inPersistence(task as Callable<T>))
	}

	def <T> Future<T> submitWithoutPersistence(Callable<T> task) {
		executor.submit(task)
	}

	Future<?> submit(Runnable task) {
		executor.submit(inPersistence(task))
	}

	Future<?> submitWithoutPersistence(Runnable task) {
		executor.submit(task)
	}

	def <T> Future<T> submit(Runnable task, T result) {
		executor.submit(inPersistence(task), result)
	}

	def <T> Future<T> submitWithoutPersistence(Runnable task, T result) {
		executor.submit(task, result)
	}

	def <T> Future<T> withPersistence(Closure<T> task) {
        executor.submit(inPersistence(task as Callable<T>))
	}

	def <T> Future<T> withoutPersistence(Closure<T> task) {
		executor.submit(task as Callable<T>)
	}

	def <T> Future<T> leftShift(Closure<T> task) {
		withPersistence(task)
	}

	def <T> Callable<T> inPersistence(Closure<T> task) {
		inPersistence(task as Callable<T>)
	}

	def <T> Callable<T> inPersistence(Callable<T> task) {
		Assert.state(persistenceInterceptor != null,
			"Unable to create persistence context wrapped callable because persistenceInterceptor is null")

		new PersistenceContextCallableWrapper(persistenceInterceptor, task)
	}

	Runnable inPersistence(Runnable task) {
		Assert.state(persistenceInterceptor != null,
			"Unable to create persistence context wrapped runnable because persistenceInterceptor is null")

		new PersistenceContextRunnableWrapper(persistenceInterceptor, task)
	}

	void destroy() {
		executor.shutdown()
		if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.warn "ExecutorService did not shutdown in 5 seconds. Forcing shutdown of any scheduled tasks"
			executor.shutdownNow()
		}
	}

}
