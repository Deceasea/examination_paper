package dbService;

import java.sql.*;

public class DBService {
    // Строка подключения к базе данных
    private static final String CONNECTION_STRING = "jdbc:postgresql://dpg-cn1542en7f5s73fdrigg-a.frankfurt-postgres.render.com/x_clients_xxet";
    // Имя пользователя для подключения к базе данных
    private static final String USER = "x_clients_user";
    // Пароль для подключения к базе данных
    private static final String PASSWORD = "x7ngHjC1h08a85bELNifgKmqZa8KIR40";

    // Метод для добавления нового сотрудника
    public static void addEmployee(String firstName, String middleName, String lastName, String phone, String email,
                                   Date birthdate, int companyId, boolean isActive) {
        String sql = "INSERT INTO employee (first_name, middle_name, last_name, phone, email, birthdate, company_id, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, middleName);
            statement.setString(3, lastName);
            statement.setString(4, phone);
            statement.setString(5, email);
            statement.setDate(6, birthdate);
            statement.setInt(7, companyId);
            statement.setBoolean(8, isActive);
            int rowsAffected = statement.executeUpdate();
            System.out.println("Added " + rowsAffected + " employee(s)");
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
        }
    }

    // Метод для просмотра всех сотрудников
    public static void viewAllEmployees() {
        String sql = "SELECT * FROM employee";
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                System.out.println(id + "-" + firstName);
            }
        } catch (SQLException e) {
            System.err.println("Error viewing all employees: " + e.getMessage());
        }
    }

    // Метод для просмотра информации о сотруднике по идентификатору
    public static void viewEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employee WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String firstName = resultSet.getString("first_name");
                    int companyId = resultSet.getInt("company_id");
                    System.out.println(id + "-" + firstName + companyId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing employee by id: " + e.getMessage());
        }
    }

    // Метод для удаления сотрудника по идентификатору
    public static void deleteEmployeeById(int employeeId) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            int rowsAffected = statement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " employee(s)");
        } catch (SQLException e) {
            System.err.println("Error deleting employee by id: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Добавление нового сотрудника
            addEmployee("Arthur", "Doylee", "Conan", "345-789", "arthurio@gmail.com", Date.valueOf("1859-09-12"), 830, true);
            // Просмотр списка сотрудников
            viewAllEmployees();
            // Поиск сотрудника по идентификатору
            int employeeId = 1; // Можно указать идентификатор сотрудника для поиска
            viewEmployeeById(employeeId);
            // Удаление сотрудника по идентификатору
            deleteEmployeeById(employeeId);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}