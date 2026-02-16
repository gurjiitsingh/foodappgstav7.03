package com.it10x.foodappgstav7_03.data.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_03.data.pos.dao.ProductDao
import com.it10x.foodappgstav7_03.data.pos.entities.ProductEntity
import kotlinx.coroutines.flow.*

class ProductsLocalViewModel(
    private val dao: ProductDao
) : ViewModel() {

    // ---------------- SEARCH STATE ----------------
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    // ---------------- PUBLIC PRODUCTS FLOW ----------------
    val products: StateFlow<List<ProductEntity>> =
        combine(_searchQuery, _selectedCategory) { query, category ->
            query.trim() to category
        }.flatMapLatest { (query, category) ->

            when {
                query.isNotEmpty() -> {
                    val isNumeric = query.all { it.isDigit() }

                    if (isNumeric) {
                        // Numeric search: use DAO code search
                        dao.searchExactCodeWithFoodType(query, null)
                    } else {
                        // Text search: get all products and filter manually
                        dao.getAll().map { allProducts ->
                            val lowerQuery = query.lowercase()

                            // Step 1: iterate each product and its words in order
                            allProducts.filter { product ->
                                val words = product.name.split(" ")
                                words.any { word ->
                                    word.lowercase().startsWith(lowerQuery)
                                }
                            }
                        }
                    }
                }

                category != null -> dao.getByCategory(category)
                else -> dao.getAll()
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )





//    val products: StateFlow<List<ProductEntity>> =
//        combine(_searchQuery, _selectedCategory) { query, category ->
//            query.trim() to category
//        }.flatMapLatest { (query, category) ->
//
//            when {
//                query.isNotEmpty() -> {
//                    val isNumeric = query.all { it.isDigit() }
//
//                    if (isNumeric) {
//                        // Numeric query: search by code
//                        dao.searchExactCodeWithFoodType(query, null)
//                    } else {
//                        // Text query: search in first word first
//                        dao.getAll().map { allProducts ->
//                            val lowerQuery = query.lowercase()
//
//                            // 🔹 Step 1: match first word
//                            val firstWordMatches = allProducts.filter { product ->
//                                product.name.split(" ").firstOrNull()
//                                    ?.lowercase()
//                                    ?.contains(lowerQuery) == true
//                            }
//
//                            if (firstWordMatches.isNotEmpty()) {
//                                firstWordMatches
//                            } else {
//                                // 🔹 Step 2: match remaining words
//                                allProducts.filter { product ->
//                                    product.name.split(" ").drop(1)
//                                        .any { it.lowercase().contains(lowerQuery) }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                category != null -> dao.getByCategory(category)
//
//                else -> dao.getAll()
//            }
//        }
//            .stateIn(
//                viewModelScope,
//                SharingStarted.WhileSubscribed(5000),
//                emptyList()
//            )

    // ---------------- FUNCTIONS ----------------
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }
}
