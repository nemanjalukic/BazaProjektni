/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.etfbl.bp.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Poslovnica;

public class PocetnaController implements Initializable {

    @FXML
    private ComboBox<String> poslovnicaComboBox;
    @FXML
    private Button prijavaButton;

    private CallableStatement cs;
    private ResultSet rs;
    private ObservableList<String> poslovnice;
    private ArrayList<Poslovnica> pos=new ArrayList<>();
    public static int ID_PRODAVNICE;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poslovnice = FXCollections.observableArrayList();
        poslovnicaComboBox.setItems(poslovnice);
        poslovnice.addAll(getPoslovnice());

        prijavaButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                try {
                    String pom=poslovnicaComboBox.getSelectionModel().getSelectedItem();
                    for(Poslovnica po:pos){
                    if(po.getNaziv().equals(pom)){
                    ID_PRODAVNICE=po.getId();
                    }
                    }
                    Stage s = new Stage();
                    Pane myPane = (Pane) FXMLLoader.load(getClass().getResource("/resources/fxml/main.fxml"));
                    Scene myScene = new Scene(myPane);
                    s.setScene(myScene);
                    s.show();
                    Node source = (Node) e.getSource();
                    Stage stage = (Stage) source.getScene().getWindow();
                    stage.close();
                } catch (IOException ex) {
                    Logger.getLogger(PocetnaController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        );

    }

    private ObservableList<String> getPoslovnice() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getPoslovnice()");
            rs = cs.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(2));
                pos.add(new Poslovnica(rs.getInt(1),rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
        return list;
    }

}
