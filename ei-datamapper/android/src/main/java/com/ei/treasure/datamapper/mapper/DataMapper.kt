package com.ei.treasure.datamapper.mapper

import com.ei.datamapping.annotation.Mapper
import com.ei.treasure.datamapper.model.Book
import com.ei.treasure.datamapper.model.OldBook

@Mapper
interface DataMapper {
    fun convert(src: OldBook): Book
}

