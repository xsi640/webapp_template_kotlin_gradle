package org.hibernate.dialect


import org.hibernate.ScrollMode
import org.hibernate.dialect.function.*
import org.hibernate.dialect.identity.IdentityColumnSupport
import org.hibernate.dialect.identity.SQLiteDialectIdentityColumnSupport
import org.hibernate.dialect.pagination.AbstractLimitHandler
import org.hibernate.dialect.pagination.LimitHandler
import org.hibernate.dialect.pagination.LimitHelper
import org.hibernate.dialect.unique.DefaultUniqueDelegate
import org.hibernate.dialect.unique.UniqueDelegate
import org.hibernate.engine.spi.RowSelection
import org.hibernate.exception.DataException
import org.hibernate.exception.JDBCConnectionException
import org.hibernate.exception.LockAcquisitionException
import org.hibernate.exception.spi.SQLExceptionConversionDelegate
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter
import org.hibernate.internal.util.JdbcExceptionHelper
import org.hibernate.mapping.Column
import org.hibernate.type.StandardBasicTypes
import java.sql.SQLException
import java.sql.Types


class SQLiteDialect : Dialect() {
    private val uniqueDelegate: UniqueDelegate

    init {
        registerColumnType(Types.BIT, "boolean")
        //registerColumnType(Types.FLOAT, "float");
        //registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.DECIMAL, "decimal")
        registerColumnType(Types.CHAR, "char")
        registerColumnType(Types.LONGVARCHAR, "longvarchar")
        registerColumnType(Types.TIMESTAMP, "datetime")
        registerColumnType(Types.BINARY, "blob")
        registerColumnType(Types.VARBINARY, "blob")
        registerColumnType(Types.LONGVARBINARY, "blob")

        registerFunction("concat", VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""))
        registerFunction("mod", SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 % ?2"))
        registerFunction("quote", StandardSQLFunction("quote", StandardBasicTypes.STRING))
        registerFunction("random", NoArgSQLFunction("random", StandardBasicTypes.INTEGER))
        registerFunction("round", StandardSQLFunction("round"))
        registerFunction("substr", StandardSQLFunction("substr", StandardBasicTypes.STRING))
        registerFunction("trim", object : AbstractAnsiTrimEmulationFunction() {
            override fun resolveBothSpaceTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "trim(?1)")
            }

            override fun resolveBothSpaceTrimFromFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "trim(?2)")
            }

            override fun resolveLeadingSpaceTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(?1)")
            }

            override fun resolveTrailingSpaceTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "rtrim(?1)")
            }

            override fun resolveBothTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "trim(?1, ?2)")
            }

            override fun resolveLeadingTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(?1, ?2)")
            }

            override fun resolveTrailingTrimFunction(): SQLFunction {
                return SQLFunctionTemplate(StandardBasicTypes.STRING, "rtrim(?1, ?2)")
            }
        })
        uniqueDelegate = SQLiteUniqueDelegate(this)
    }

    override fun getIdentityColumnSupport(): IdentityColumnSupport {
        return IDENTITY_COLUMN_SUPPORT
    }

    override fun getLimitHandler(): LimitHandler {
        return LIMIT_HANDLER
    }

    // lock acquisition support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    override fun supportsLockTimeouts(): Boolean {
        // may be http://sqlite.org/c3ref/db_mutex.html ?
        return false
    }

    override fun getForUpdateString(): String {
        return ""
    }

    override fun supportsOuterJoinForUpdate(): Boolean {
        return false
    }

    // current timestamp support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    override fun supportsCurrentTimestampSelection(): Boolean {
        return true
    }

    override fun isCurrentTimestampSelectStringCallable(): Boolean {
        return false
    }

    override fun getCurrentTimestampSelectString(): String {
        return "select current_timestamp"
    }

    override fun buildSQLExceptionConversionDelegate(): SQLExceptionConversionDelegate {
        return SQLExceptionConversionDelegate { sqlException, message, sql ->
            val errorCode = JdbcExceptionHelper.extractErrorCode(sqlException) and 0xFF
            if (errorCode == SQLITE_TOOBIG || errorCode == SQLITE_MISMATCH) {
                return@SQLExceptionConversionDelegate DataException(message, sqlException, sql)
            } else if (errorCode == SQLITE_BUSY || errorCode == SQLITE_LOCKED) {
                return@SQLExceptionConversionDelegate LockAcquisitionException(message, sqlException, sql)
            } else if (errorCode >= SQLITE_IOERR && errorCode <= SQLITE_PROTOCOL || errorCode == SQLITE_NOTADB) {
                return@SQLExceptionConversionDelegate JDBCConnectionException(message, sqlException, sql)
            }

            // returning null allows other delegates to operate
            null
        }
    }

    override fun getViolatedConstraintNameExtracter(): ViolatedConstraintNameExtracter {
        return EXTRACTER
    }

    // union subclass support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    override fun supportsUnionAll(): Boolean {
        return true
    }

    // DDL support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    override fun canCreateSchema(): Boolean {
        return false
    }

    override fun hasAlterTable(): Boolean {
        // As specified in NHibernate dialect
        return false
    }

    override fun dropConstraints(): Boolean {
        return false
    }

    override fun qualifyIndexName(): Boolean {
        return false
    }

    override fun getAddColumnString(): String {
        return "add column"
    }

    override fun getDropForeignKeyString(): String {
        throw UnsupportedOperationException("No drop foreign key syntax supported by SQLiteDialect")
    }

    override fun getAddForeignKeyConstraintString(constraintName: String?,
                                                  foreignKey: Array<String>, referencedTable: String?, primaryKey: Array<String>,
                                                  referencesPrimaryKey: Boolean): String {
        throw UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect")
    }

    override fun getAddPrimaryKeyConstraintString(constraintName: String?): String {
        throw UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect")
    }

    override fun supportsCommentOn(): Boolean {
        return true
    }

    override fun supportsIfExistsBeforeTableName(): Boolean {
        return true
    }

    /* not case insensitive for unicode characters by default (ICU extension needed)
	public boolean supportsCaseInsensitiveLike() {
    return true;
  }
  */

    override fun doesReadCommittedCauseWritersToBlockReaders(): Boolean {
        // TODO Validate (WAL mode...)
        return true
    }

    override fun doesRepeatableReadCauseReadersToBlockWriters(): Boolean {
        return true
    }

    override fun supportsTupleDistinctCounts(): Boolean {
        return false
    }

    override fun getInExpressionCountLimit(): Int {
        // Compile/runtime time option: http://sqlite.org/limits.html#max_variable_number
        return 1000
    }

    override fun getUniqueDelegate(): UniqueDelegate {
        return uniqueDelegate
    }

    private class SQLiteUniqueDelegate(dialect: Dialect) : DefaultUniqueDelegate(dialect) {
        override fun getColumnDefinitionUniquenessFragment(column: Column): String {
            return " unique"
        }
    }

    override fun getSelectGUIDString(): String {
        return "select hex(randomblob(16))"
    }

    override fun defaultScrollMode(): ScrollMode {
        return ScrollMode.FORWARD_ONLY
    }

    companion object {

        // database type mapping support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /*@Override
	public String getCastTypeName(int code) {
		// http://sqlite.org/lang_expr.html#castexpr
		return super.getCastTypeName( code );
	}*/

        // IDENTITY support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        private val IDENTITY_COLUMN_SUPPORT = SQLiteDialectIdentityColumnSupport()

        // limit/offset support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        private val LIMIT_HANDLER = object : AbstractLimitHandler() {
            override fun processSql(sql: String?, selection: RowSelection?): String {
                val hasOffset = LimitHelper.hasFirstRow(selection)
                return sql!! + if (hasOffset) " limit ? offset ?" else " limit ?"
            }

            override fun supportsLimit(): Boolean {
                return true
            }

            override fun bindLimitParametersInReverseOrder(): Boolean {
                return true
            }
        }

        // SQLException support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        private val SQLITE_BUSY = 5
        private val SQLITE_LOCKED = 6
        private val SQLITE_IOERR = 10
        private val SQLITE_CORRUPT = 11
        private val SQLITE_NOTFOUND = 12
        private val SQLITE_FULL = 13
        private val SQLITE_CANTOPEN = 14
        private val SQLITE_PROTOCOL = 15
        private val SQLITE_TOOBIG = 18
        private val SQLITE_CONSTRAINT = 19
        private val SQLITE_MISMATCH = 20
        private val SQLITE_NOTADB = 26

        private val EXTRACTER = object : TemplatedViolatedConstraintNameExtracter() {
            @Throws(NumberFormatException::class)
            override fun doExtractConstraintName(sqle: SQLException): String? {
                val errorCode = JdbcExceptionHelper.extractErrorCode(sqle) and 0xFF
                return if (errorCode == SQLITE_CONSTRAINT) {
                    extractUsingTemplate("constraint ", " failed", sqle.message)
                } else null
            }
        }
    }
}