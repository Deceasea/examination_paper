package db;

import model.Employee;

import java.sql.SQLException;

public interface EmployeeRep {
    /**
     * Создание сотрудника в базе данных.
     *
     * @param employee Сотрудник для добавления.
     * @return Идентификатор нового сотрудника.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    int createEmployeeDB(Employee employee) throws SQLException;

    /**
     * Получение сотрудника из базы данных по его идентификатору.
     *
     * @param id Идентификатор сотрудника.
     * @return Сотрудник с указанным идентификатором.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    Employee getEmployeeByIdDB(int id) throws SQLException;

    /**
     * Удаление сотрудника из базы данных по его идентификатору.
     *
     * @param id Идентификатор сотрудника.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    void deleteEmployeeByIdDB(int id) throws SQLException;

    /**
     * Создание сотрудника.
     *
     * @param employee Сотрудник для добавления.
     * @return Идентификатор нового сотрудника.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    int createEmployee(Employee employee) throws SQLException;

    /**
     * Получение сотрудника по его идентификатору.
     *
     * @param id Идентификатор сотрудника.
     * @return Сотрудник с указанным идентификатором.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    Employee getEmployeeById(int id) throws SQLException;

    /**
     * Удаление сотрудника по его идентификатору.
     *
     * @param id Идентификатор сотрудника.
     * @throws SQLException Если возникает ошибка при работе с базой данных.
     */
    void deleteEmployeeById(int id) throws SQLException;
}