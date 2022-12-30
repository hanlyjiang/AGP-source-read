package com.github.hanlyjiang.app.model

/**
 * YmRepo
 *
 * @author jiang.hanghang 2022/12/30 11:56
 * @version 1.0
 *
 */
class YmRepo : IYmRepository {

    private val ymList: MutableList<Ym> = ArrayList()

    override fun getAll(): List<Ym> {
        return ymList
    }

    override fun getById(id: String): Ym? {
        return ymList.singleOrNull { ym ->
            ym.id == id
        }
    }

    override fun add(ym: Ym): Boolean {
        return ymList.add(ym)
    }

}