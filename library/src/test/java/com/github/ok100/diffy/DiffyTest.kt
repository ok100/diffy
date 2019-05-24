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

import com.nhaarman.mockitokotlin2.*
import org.junit.Test

internal class DiffyTest {

    private val onFooChanged: (String) -> Unit = mock()
    private val onBarChanged: (Int) -> Unit = mock()
    private val onBazChanged: (Boolean?) -> Unit = mock()

    private val diffy = Diffy<State>()
        .observe(State::foo, onFooChanged)
        .observe(State::bar, onBarChanged)
        .observe(State::baz, onBazChanged)

    @Test
    fun `when created, then no callback`() {
        verify(onFooChanged, never()).invoke(any())
        verify(onBarChanged, never()).invoke(any())
        verify(onBazChanged, never()).invoke(any())
    }

    @Test
    fun `when first state, then all callbacks`() {
        diffy.diff(State("a", 1, null))
        verify(onFooChanged).invoke("a")
        verify(onBarChanged).invoke(1)
        verify(onBazChanged).invoke(null)
    }

    @Test
    fun `when non-null value changes, then correct callback`() {
        diffy.diff(State("a", 1, null))
        diffy.diff(State("b", 1, null))
        verify(onFooChanged).invoke("a")
        verify(onFooChanged).invoke("b")
        verify(onBarChanged, times(1)).invoke(any())
        verify(onBazChanged, times(1)).invoke(anyOrNull())
    }

    @Test
    fun `when nullable value changes, then correct callback`() {
        diffy.diff(State("a", 1, null))
        diffy.diff(State("a", 1, true))
        verify(onFooChanged, times(1)).invoke(any())
        verify(onBarChanged, times(1)).invoke(any())
        verify(onBazChanged).invoke(null)
        verify(onBazChanged).invoke(true)
    }
}

private data class State(
    val foo: String,
    val bar: Int,
    val baz: Boolean?
)