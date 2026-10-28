package dataaccess;

import dataaccess.sql.SqlAuthDAO;
import dataaccess.sql.SqlGameDAO;
import dataaccess.sql.SqlUserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatabaseSetupTest {

    @Test
    public void createsAllTables() {
        Assertions.assertDoesNotThrow(SqlUserDAO::new);
        Assertions.assertDoesNotThrow(SqlAuthDAO::new);
        Assertions.assertDoesNotThrow(SqlGameDAO::new);
    }
}