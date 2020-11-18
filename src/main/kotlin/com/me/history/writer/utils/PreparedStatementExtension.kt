package com.me.history.writer.utils

import com.google.protobuf.Timestamp
import java.sql.PreparedStatement
import java.sql.Types

fun PreparedStatement.setNullableString(index: Int, value: String?) {
    if (value.isNullOrEmpty()) {
        this.setNull(index, Types.VARCHAR)
    } else {
        this.setString(index, value)
    }
}

fun PreparedStatement.setNullableTimestamp(index: Int, value: Timestamp?) {
    if (value == null) {
        this.setNull(index, Types.TIMESTAMP)
    } else {
        this.setTimestamp(index, value.toSqlTimestamp())
    }
}
fun PreparedStatement.setNullableInt(index: Int, value: Int?) {
    if (value == null) {
        this.setNull(index, Types.INTEGER)
    } else {
        this.setInt(index, value)
    }
}

fun PreparedStatement.setNullableLong(index: Int, value: Long?) {
    if (value == null) {
        this.setNull(index, Types.BIGINT)
    } else {
        this.setLong(index, value)
    }
}