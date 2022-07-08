package com.example.tetrisproject.helper

fun array2dOfBytes(sizeOuter: Int, sizeInner: Int): Array<ByteArray>
                = Array(sizeOuter) {ByteArray(sizeInner)}
