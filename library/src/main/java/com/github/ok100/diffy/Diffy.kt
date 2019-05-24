/**
 * Copyright 2019 Ondrej Kipila (@ok100)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ok100.diffy

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Using a single state architecture, when a single value in state is changed, all values
 * would be re-rendered. This can lead to some performance or other issues.
 *
 * Using [Diffy], every value can be observed for a change.
 *
 * Example usage:
 *
 * ```
 * Diffy.with(this, viewModel.state)
 *     .observe(State::foo) { fooTextView.text = it }
 *     .observe(State::bar) { barTextView.text = it }
 * ```
 */
@Suppress("unused")
class Diffy<S> {

    private var oldState: S? = null
    private val observers = mutableListOf<DiffyObserver<S, Any?>>()

    /**
     * Registers a new state observer.
     * @param selector selects an attribute to observe.
     * @param onChange called when the attribute value changes.
     * @return current [Diffy] instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> observe(selector: (S) -> T, onChange: (T) -> Unit): Diffy<S> {
        observers += DiffyObserver(selector, onChange) as DiffyObserver<S, Any?>
        return this
    }

    /**
     * Calculates changes in [newState] and notifies observers.
     */
    fun diff(newState: S) {
        observers.forEach { diff(oldState, newState, it) }
        oldState = newState
    }

    private fun <T> diff(oldState: S?, newState: S, observer: DiffyObserver<S, T>) {
        val oldValue = if (oldState == null) null else observer.selector(oldState)
        val newValue = observer.selector(newState)
        if (oldState == null || oldValue != newValue) {
            observer.onChange(newValue)
        }
    }

    companion object {
        /**
         * Helper method to set up [stateLiveData] with [Diffy].
         * [stateLiveData] are automatically observed using the given [owner].
         * @return new [Diffy] instance.
         */
        fun <S> with(owner: LifecycleOwner, stateLiveData: LiveData<S>): Diffy<S> {
            return Diffy<S>().also { diffy ->
                stateLiveData.observe(owner, Observer { diffy.diff(it) })
            }
        }
    }
}

internal data class DiffyObserver<S, T>(
    val selector: (S) -> T,
    val onChange: (T) -> Unit
)