package dbclasses;

import commonclasses.authmessages.SuccessfulAuth;
import commonclasses.messages.Message;
import commonclasses.warningmessages.WarningMessageAuth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AuthUsers {

    private Connection connection;
    private List<String> authorizedUsers;

    public List<String> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public static AuthUsers authUsersInstance = new AuthUsers();

    public static AuthUsers getInstance() {
        return authUsersInstance;
    }
    private AuthUsers() {
        init();
    }

    private void init() {
        connection = new Database().connect();
        authorizedUsers = new ArrayList<>();
    }


    @SneakyThrows
    public Message authorize(String login, String password) {
        String query_select_login = String.format("SELECT login FROM users WHERE login = '%s'", login);
        String query_select_login_password = String.format("SELECT login FROM users WHERE login = '%s' AND password = '%s'", login, password);
        String query_select_all = String.format("SELECT login FROM users WHERE login = '%s' AND password = '%s' AND online = 0", login, password);
        String query_update = String.format("UPDATE users SET online = '1' WHERE (login = '%s');", login);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query_select_login);
        if (resultSet.next()) {
            resultSet = statement.executeQuery(query_select_login_password);
            if (resultSet.next()) {
                resultSet = statement.executeQuery(query_select_all);
                if (resultSet.next()) {
                    authorizedUsers.add(login);
                    statement.executeUpdate(query_update);
                    statement.close();
                    resultSet.close();
                    return new SuccessfulAuth();
                } else {
                    log.debug("This user already authorized");
                    statement.close();
                    resultSet.close();
                    return new WarningMessageAuth("This user already authorized");
                }
            } else {
                log.debug("Incorrect password");
                statement.close();
                resultSet.close();
                return new WarningMessageAuth("Incorrect login or password");
            }
        } else {
            log.debug("Incorrect login");
            statement.close();
            resultSet.close();
            return new WarningMessageAuth("Incorrect login or password");
        }

    }

    @SneakyThrows
    public boolean register(String login, String password){
        String query_insert = String.format("INSERT INTO users (login, password) VALUES ('%s','%s')",login, password);
        Statement statement = connection.createStatement();
        if (statement.executeUpdate(query_insert)==1){
            return true;
        }else {
            return false;
        }
    }


    @SneakyThrows
    public void disconnect(String login) {
        String query_update = String.format("UPDATE users SET online = '0' WHERE login = '%s'",login);
        Statement statement = connection.createStatement();
        statement.executeUpdate(query_update);
        statement.close();

    }

    @SneakyThrows
    public void disconnectAll() {
        String query_update = String.format("UPDATE users SET online = '0' ");
        Statement statement = connection.createStatement();
        statement.executeUpdate(query_update);
        statement.close();
    }
}
