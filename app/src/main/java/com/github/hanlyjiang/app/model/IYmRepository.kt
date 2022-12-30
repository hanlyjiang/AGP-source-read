package com.github.hanlyjiang.app.model

/**
 * YmRepository
 * @author jiang.hanghang 2022/12/30 11:50
 * @version 1.0
 *
 */
interface IYmRepository {

    fun getAll(): List<Ym>

    fun getById(id: String): Ym?

    fun add(ym: Ym): Boolean

}