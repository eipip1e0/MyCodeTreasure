// SOURCE
package com.ei.treasure

import com.ei.datamapping.annotation.Mapper

@Mapper
interface DataMapper {
    fun convert(src: OldBook): Book
}

data class OldBook(
    val name: String = "oldName",
    val id: Int = -1,
    val publisher: OldPublisher = OldPublisher()
)

data class OldPublisher(
    val name: String = "oldName",
    val address: String = "oldAddress"
)

data class Publisher(
    val name: String = "null",
    val address: String = "null"
)

data class Book(
    val name: String = "new",
    val id: Int = -1,
    val publisher: Publisher = Publisher()
)

// EXPECT
// FILE: Company$$DeepCopy.kt
package com.bennyhuo.kotlin.deepcopy.sample

import com.bennyhuo.kotlin.deepcopy.sample.deepCopy
import kotlin.String
import kotlin.jvm.JvmOverloads

@JvmOverloads
public fun Company.deepCopy(
    name: String = this.name,
    location: Location = this.location,
    district: District = this.district,
): Company = Company(name, location.deepCopy(), district.deepCopy())