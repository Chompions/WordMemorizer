package com.sawelo.wordmemorizer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.util.callback.CategoryDiffUtilCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DifferTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testingDiffer() = runTest {
        val callback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                println("Inserting $count stuff at $position")
            }

            override fun onRemoved(position: Int, count: Int) {
                println("Removing $count stuff at $position")
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                println("Moving from $fromPosition to $toPosition")
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                println("Changing $count stuff at $position")
            }
        }

        val config = AsyncDifferConfig.Builder(CategoryDiffUtilCallback).build()
        val differ = AsyncListDiffer(callback, config)

        val firstList = listOf(
            Category(0, "Zero")
        )
        val secondList = listOf(
            Category(0, "Zero"),
            Category(1, "One"),
        )
        val thirdList = listOf(
            Category(1, "One"),
        )

        val flow = flow {
            emit(firstList)
            println("First")
            delay(1000L)
            emit(null)
            emit(secondList)
            println("Second")
            delay(1000L)
            emit(thirdList)
            println("Third")
        }

        flow.collect { categoryList ->
            val x = mutableListOf<Category>().apply {
                if (categoryList != null) {
                    println("ADDING")
                    addAll(categoryList)
                }
            }
            differ.submitList(x) {
                println("Update")
            }
        }
//
//
//        differ.submitList(secondList.map { it.copy() }) {
//            println("Update second")
//        }
//        differ.submitList(thirdList) {
//            println("Update third")
//        }
//        advanceUntilIdle()
    }
}