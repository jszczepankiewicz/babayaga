package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.sql.ColumnType.*
import com.github.jszczepankiewicz.babayaga.sql.PostgresqlDBDialect
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.assertThat


class PostgresqlDBDialectSpec : Test({

    val dialect = PostgresqlDBDialect()

    describe("Supports generating DDL for Postgresql column") {

        test("of TEXT NULLABLE") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameForText", TEXT), true)).isEqualTo("somecolumnnamefortext TEXT")
        }

        test("of TIMESTAMP_WITHOUT_TZ NULLABLE") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameTs", TIMESTAMP_WITHOUT_TZ), true)).isEqualTo("somecolumnnamets TIMESTAMP WITHOUT TIME ZONE")
        }

        test("of BOOL NULLABLE") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameBool", BOOL), true)).isEqualTo("somecolumnnamebool BOOLEAN")
        }

        test("of BINARY NULLABLE") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameBinary", BINARY), true)).isEqualTo("somecolumnnamebinary BYTEA")
        }

        test("of TEXT NOT NULL") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameForText", TEXT), false)).isEqualTo("somecolumnnamefortext TEXT NOT NULL")
        }

        test("of TIMESTAMP_WITHOUT_TZ NOT NULL") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameTs", TIMESTAMP_WITHOUT_TZ), false)).isEqualTo("somecolumnnamets TIMESTAMP WITHOUT TIME ZONE NOT NULL")
        }

        test("of BOOL NOT NULL") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameBool", BOOL), false)).isEqualTo("somecolumnnamebool BOOLEAN NOT NULL")
        }

        test("of BINARY NOT NULL") {
            assertThat(dialect.getColumnDDL(Pair("someColumnNameBinary", BINARY), false)).isEqualTo("somecolumnnamebinary BYTEA NOT NULL")
        }
    }

    describe("Supports generating DDL for index") {
        val multipleAttributes = listOf(
                Pair("name", TEXT),
                Pair("born", TIMESTAMP_WITHOUT_TZ),
                Pair("married", BOOL),
                Pair("picture", BINARY))

        test("with single column") {
            assertThat(dialect.buildCreateIndexTableDDL("index_veryfamousartists_on_name", listOf(Pair("name", TEXT))))
                    .isEqualTo("CREATE TABLE index_veryfamousartists_on_name(name TEXT NOT NULL,id BYTEA NOT NULL UNIQUE,PRIMARY KEY(name,id))")
        }

        test("with multiple columns") {
            assertThat(dialect.buildCreateIndexTableDDL("index_veryfamousartists_on_name", multipleAttributes))
                    .isEqualTo("CREATE TABLE index_veryfamousartists_on_name(name TEXT NOT NULL,born TIMESTAMP WITHOUT TIME ZONE NOT NULL," +
                            "married BOOLEAN NOT NULL,picture BYTEA NOT NULL,id BYTEA NOT NULL UNIQUE,PRIMARY KEY(name,born,married,picture,id))")
        }
    }
})