package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.CreateIndexStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.util.StringUtils;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

import java.util.Arrays;
import java.util.Iterator;

public class CreateIndexGenerator implements SqlGenerator<CreateIndexStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(CreateIndexStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(CreateIndexStatement createIndexStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", createIndexStatement.getTableName());
        validationErrors.checkRequiredField("columns", createIndexStatement.getColumns());
        return validationErrors;
    }

    public Sql[] generateSql(CreateIndexStatement statement, Database database) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("CREATE ");
        if (statement.isUnique() != null && statement.isUnique()) {
            buffer.append("UNIQUE ");
        }
        buffer.append("INDEX ");

        buffer.append(database.escapeIndexName(null, statement.getIndexName())).append(" ON ");
        buffer.append(database.escapeTableName(statement.getTableSchemaName(), statement.getTableName())).append("(");
        Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
        while (iterator.hasNext()) {
            String column = iterator.next();
            buffer.append(database.escapeColumnName(statement.getTableSchemaName(), statement.getTableName(), column));
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
                buffer.append(" ON ").append(statement.getTablespace());
            } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
                buffer.append(" IN ").append(statement.getTablespace());
            } else {
                buffer.append(" TABLESPACE ").append(statement.getTablespace());
            }
        }

        return new Sql[]{new UnparsedSql(buffer.toString())};
    }
}
