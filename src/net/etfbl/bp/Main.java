package net.etfbl.bp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import net.etfbl.bp.helper.ConnectionPool;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;


public class Main extends Application {
    

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/pocetna.fxml"));
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> ConnectionPool.close());
        } catch (Exception e) {
            showAlertException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Greška!");
        alert.setTitle("Greška!");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/main.css");
        alert.show();
    }

    public static void showAlertWarining(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Upozorenje!");
        alert.setTitle("Upozorenje!");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/main.css");
        alert.show();
    }

    public static void showAlertException(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(e.getClass().getName());
        alert.setContentText(e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();
        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);

        try {
            sw.close();
            pw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        alert.showAndWait();
    }



 public String evidencijaRadaNaProjektu(String JMB, int idProjekta, int brojSati){
     Connection c = null;
     CallableStatement cs = null;
     String result = "";
     try {
         c = DriverManager.getConnection("jdbc:mysql://localhost:3306/pismeni", "referent", "reFErent");
         cs = c.prepareCall("call evidencijaRadaNaProjektu(?, ?, ?, ?, ?)");
         cs.setString(1, JMB);
         cs.setInt(2, idProjekta);
         cs.setInt(3, brojSati);
         cs.registerOutParameter(4, Types.BOOLEAN);
         cs.executeUpdate();
         result = cs.getString(4);
     } catch (SQLException e) {
         result = "Greška";
     } finally {
        if (c != null)
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
         if (cs != null)
             try {
                 cs.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
     }
     return result;
 }















}