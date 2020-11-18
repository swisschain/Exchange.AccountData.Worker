package com.me.history.writer.utils

import com.google.protobuf.Timestamp
import java.time.Instant
import java.util.Date

fun Timestamp.toSqlTimestamp(): java.sql.Timestamp{
    return java.sql.Timestamp.from(Instant.ofEpochSecond(this.seconds, this.nanos.toLong()))
}

fun Date.createProtobufTimestampBuilder(): Timestamp.Builder {
    val instant = this.toInstant()
    return Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
}