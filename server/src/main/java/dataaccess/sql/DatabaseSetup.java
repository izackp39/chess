package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.SQLException;

class DatabaseSetup {

    private DatabaseSetup() {
    }

    static void createTable(String createStatement) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(createStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create table", ex);
        }
    }
}