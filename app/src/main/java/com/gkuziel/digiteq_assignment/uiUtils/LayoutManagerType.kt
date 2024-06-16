package com.gkuziel.digiteq_assignment.uiUtils


sealed class LayoutManagerType(val id: Int) {
    class Mesh : LayoutManagerType(1)
    class Linear : LayoutManagerType(2)
    class Grid : LayoutManagerType(3)

    companion object {
        fun getManager(id: Int): LayoutManagerType {
            return when (id) {
                1 -> Mesh()
                2 -> Linear()
                3 -> Grid()
                else -> {
                    throw Exception("invalid id")
                }
            }
        }
    }
}