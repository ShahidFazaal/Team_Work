package controller;

import DBConnection.DBConnection;
import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public class ViewTodoListController {
    public JFXButton btnNewToDO;
    public ListView<String> txtToDoList;
    public TextArea txtDescription;
    public JFXButton btnDelete;
    public Label lblDate;
    public AnchorPane root;
    public JFXButton btnToDayList;
    public JFXButton btnAll;

    public void initialize(){
        loadToDoList();

        txtToDoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Connection connection = DBConnection.getInstance().getConnection();
                String value = txtToDoList.getSelectionModel().getSelectedItem();
                if (newValue==null) {
                    return;
                }
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select description,dueDate from todolist where title = '"+value+"'");

                    if(resultSet.next()){
                        txtDescription.setText(resultSet.getString(1));
                    }
                    lblDate.setText(resultSet.getString(2));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private void loadToDoList() {
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select title from todolist");
            ObservableList<String> items = txtToDoList.getItems();
            items.clear();

            while (resultSet.next()){
                String string = resultSet.getString(1);
                items.add(string);
            }

            txtToDoList.refresh();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void btnNewToDOOnAction(ActionEvent actionEvent) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(this.getClass().getResource("/view/CreateNewToDo.fxml")));
        Stage primaryStage = (Stage) root.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Create New To Do");
        Image image = new Image("/images/icons8-to-do-48.png");
        primaryStage.getIcons().add(image);
        primaryStage.centerOnScreen();
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
        String selectedTitle = txtToDoList.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(AlertType.WARNING, "Do you want to delete the selected one?", ButtonType.YES,
            ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            try {
                    PreparedStatement preparedStatement = DBConnection.getInstance().getConnection()
                .prepareStatement("DELETE FROM todolist WHERE title=?");
                    preparedStatement.setObject(1,selectedTitle);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows>0) {
                new Alert(AlertType.CONFIRMATION,"Deleted Successfully",ButtonType.OK).showAndWait();
                txtDescription.clear();
                lblDate.setText("");
            }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
        }
        txtToDoList.getSelectionModel().clearSelection();
        txtToDoList.getItems().clear();
        loadToDoList();


    }


    public void btnToDayListOnAction(ActionEvent actionEvent) throws SQLException {
        ObservableList<String> toDo = txtToDoList.getItems();
        ResultSet resultSet = DBConnection.getInstance().getConnection().createStatement().executeQuery("SELECT title FROM todolist WHERE dueDate = '" + LocalDate.now() + "' ");
        toDo.clear();
        while (resultSet.next()){
            toDo.add(resultSet.getString(1));
        }

    }

    public void btnAll_OnAction(ActionEvent actionEvent) throws SQLException {
        ObservableList<String> toDo = txtToDoList.getItems();
        ResultSet resultSet = DBConnection.getInstance().getConnection().createStatement().executeQuery("SELECT title FROM todolist");
        toDo.clear();
        while (resultSet.next()){
            toDo.add(resultSet.getString(1));
        }
    }
}
