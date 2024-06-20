package db;

import model.Employee;

import java.sql.*;

public class EmployeeRepJDBC implements EmployeeRep {

    // Строка подключения к базе данных
    private static final String CONNECTION_STRING = "jdbc:postgresql://dpg-cn1542en7f5s73fdrigg-a.frankfurt-postgres.render.com/x_clients_xxet";
    // Имя пользователя для подключения к базе данных
    private static final String USER = "x_clients_user";
    // Пароль для подключения к базе данных
    private static final String PASSWORD = "x7ngHjC1h08a85bELNifgKmqZa8KIR40";
    // SQL запрос для добавления сотрудника
    private static final String SQL_INSERT_EMPLOYEE = "INSERT INTO employee (first_name, last_name, phone, email, company_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
    // SQL запрос для выборки сотрудника по идентификатору
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM employee WHERE id = ?";
    // SQL запрос для удаления сотрудника по идентификатору
    private static final String SQL_DELETE_BY_ID = "DELETE FROM employee WHERE id = ?";

    // Метод для создания сотрудника в базе данных
    @Override
    public int createEmployeeDB(Employee employee) throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_EMPLOYEE)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getPhone());
            statement.setString(4, employee.getEmail());
            statement.setInt(5, employee.getCompanyId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Failed to get generated key.");
            }
        }
    }

    // Метод для получения сотрудника из базы данных по идентификатору
    @Override
    public Employee getEmployeeByIdDB(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEmployee(resultSet);
                } else {
                    throw new SQLException("Employee with id " + id + " not found.");
                }
            }
        }
    }

    // Метод для удаления сотрудника из базы данных по идентификатору
    @Override
    public void deleteEmployeeByIdDB(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Метод для отображения данных сотрудника из ResultSet в объект Employee
    private Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        employee.setId(resultSet.getInt("id"));
        employee.setFirstName(resultSet.getString("first_name"));
        employee.setMiddleName(resultSet.getString("middle_name"));
        employee.setLastName(resultSet.getString("last_name"));
        employee.setCompanyId(resultSet.getInt("company_id"));
        employee.setEmail(resultSet.getString("email"));
        employee.setPhone(resultSet.getString("phone"));
        employee.setBirthdate(resultSet.getString("birthdate"));
        employee.setActive(resultSet.getBoolean("is_active"));
        return employee;
    }

    // Методы интерфейса EmployeeRep
    @Override
    public int createEmployee(Employee employee) throws SQLException {
        return createEmployeeDB(employee);
    }

    @Override
    public Employee getEmployeeById(int id) throws SQLException {
        return getEmployeeByIdDB(id);
    }

    @Override
    public void deleteEmployeeById(int id) throws SQLException {
        deleteEmployeeByIdDB(id);
    }
}