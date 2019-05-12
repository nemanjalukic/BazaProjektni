package net.etfbl.bp.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Zaposleni;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class ZaposleniController {
    @FXML
    private Button prikaziUgovorButton;

    @FXML
    private ComboBox<String> imeComboBox;

    @FXML
    private Button obrisiButton;

    @FXML
    private Button ponistiButton;

    @FXML
    private Button dodajRacunButton;

    @FXML
    private ComboBox<String> prezimeComboBox;

    @FXML
    private Button noviZaposleniButton;

    @FXML
    private Button prikaziButton;

    @FXML
    private TableView<Zaposleni> table;

    private ObservableList<String> imena, prezimena;
    private ObservableList<Zaposleni> zaposleni;

    private PreparedStatement ps;
    private CallableStatement cs;
    private ResultSet rs;

    @FXML
    void initialize() {
        zaposleni = FXCollections.observableArrayList();
        table.setItems(zaposleni);
        pripremiKolone(table);
        imena = FXCollections.observableArrayList("*");
        imeComboBox.setItems(imena);
        prezimena = FXCollections.observableArrayList("*");
        prezimeComboBox.setItems(prezimena);

        try {
            cs = ConnectionPool.getConnection().prepareCall("SELECT Ime, Prezime FROM zaposleni");
            rs = cs.executeQuery();
            while (rs.next()) {
                imena.add(rs.getString(1));
                prezimena.add(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }

        prikaziButton.setOnAction(event -> {
            if (imeComboBox.getEditor().getText().isEmpty() && prezimeComboBox.getEditor().getText().isEmpty())
                Main.showAlertWarining("Morate unijeti ime ili prezime ili staviti *.");
            else {
                pripremiZaNoviPrikaz();
                String ime = imeComboBox.getEditor().getText().isEmpty() ? "*" : imeComboBox.getEditor().getText();
                String prezime = prezimeComboBox.getEditor().getText().isEmpty() ? "*" : prezimeComboBox.getEditor().getText();
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL getZaposleniInfo(?, ?)");
                    cs.setObject(1, ime);
                    cs.setObject(2, prezime);
                    rs = cs.executeQuery();
                    while (rs.next())
                        zaposleni.add(new Zaposleni(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getString(8)));
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs, rs);
                }

            }
        });

        noviZaposleniButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Unos novog zaposlenog");
            alert.setGraphic(null);
            VBox vBox = new VBox();
            vBox.setSpacing(10);
            alert.getDialogPane().setContent(vBox);

            List<String> list = new ArrayList<>();
            Collections.addAll(list,
                    "JMB", "Ime", "Prezime", "Adresa", "Telefon", "Plata", "Broj računa");

            for (String s : list) {
                TextField t = new TextField();
                t.setPromptText(s);
                t.setFont(new Font(13));
                t.setPrefHeight(30);
                t.setPrefWidth(360);
                vBox.getChildren().add(t);
            }

            ObservableList<String> poslovnice = getPoslovnice();
            ComboBox<String> comboBox = new ComboBox<>(poslovnice);
            comboBox.setPromptText("Poslovnica");
            comboBox.setPrefHeight(30);
            comboBox.setPrefWidth(360);
            comboBox.setStyle("-fx-font-size: 13px;");
            vBox.getChildren().add(comboBox);


            DatePicker dp1 = new DatePicker();
            dp1.setPromptText("Datum potpisivanja ugovora");
            dp1.setPrefWidth(360);
            dp1.setPrefHeight(30);
            dp1.getEditor().setFont(new Font(13));
            DatePicker dp2 = new DatePicker();
            dp2.setPromptText("Datum prekida ugovora");
            dp2.setPrefWidth(360);
            dp2.setPrefHeight(30);
            dp2.getEditor().setFont(new Font(13));


            vBox.getChildren().addAll(dp1, dp2);

            if (alert.showAndWait().get().getButtonData().isDefaultButton()) {
                try {
                    List<Node> l = vBox.getChildren();
                    Zaposleni z = new Zaposleni(
                            ((TextField) l.get(0)).getText(),
                            ((TextField) l.get(1)).getText(),
                            ((TextField) l.get(2)).getText(),
                            ((TextField) l.get(3)).getText(),
                            ((TextField) l.get(4)).getText(),
                            ((TextField) l.get(5)).getText(),
                            ((TextField) l.get(6)).getText(),
                            ((ComboBox<String>) l.get(7)).getValue(),
                            ((DatePicker) l.get(8)).getValue().toString(),
                            ((DatePicker) l.get(9)).getValue() == null ? null : ((DatePicker) l.get(10)).getValue().toString()
                    );

                    cs = ConnectionPool.getConnection().prepareCall("CALL unosZaposlenog(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    cs.setString(1, z.getJmb());
                    cs.setString(2, z.getIme());
                    cs.setString(3, z.getPrezime());
                    cs.setString(4, z.getAdresa());
                    cs.setString(5, z.getTelefon());
                    cs.setObject(6, z.getPlata());
                    cs.setString(7, z.getRacunZaposlenog());
                    cs.setString(8, z.getPoslovnica());
                    cs.setObject(9, z.getDatumPotpisivanja());
                    cs.setObject(10, z.getDatumPrekida());
                    cs.executeUpdate();
                    SQLWarning warning = cs.getWarnings();
                    if (warning != null)
                        throw new SQLException(warning);
                    zaposleni.add(z);
                    imena.add(z.getIme());
                    prezimena.addAll(z.getPrezime());
                } catch (SQLException e) {
                    Main.showAlertError(e.getMessage());
                } catch (NullPointerException e) {
                    Main.showAlertWarining("Sva polja osim zadnjeg moraju biti popunjena.");
                } catch (Exception e) {
                    Main.showAlertException(e);
                } finally {
                    ConnectionPool.close(cs);
                }
            }
        });

        ponistiButton.setOnAction(event -> {
            zaposleni.clear();
            imeComboBox.getEditor().clear();
            prezimeComboBox.getEditor().clear();
        });

        dodajRacunButton.setOnAction(event -> {
            Zaposleni z = table.getSelectionModel().getSelectedItem();
            if (z == null)
                Main.showAlertError("Morate izabrati zaposlenog iz tabele!");
            else {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setGraphic(null);
                dialog.getEditor().setTextFormatter(new TextFormatter<>(change -> (change.getText().matches("\\d*")) ? change : null));
                dialog.setHeaderText("Unos novog računa za zaposlenog: " + z.getIme() + " " + z.getPrezime() + ".");
                dialog.setContentText("Unesite broj računa: ");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(brojRacuna -> {
                    if (brojRacuna.isEmpty())
                        Main.showAlertError("Morate unijeti broj računa!");
                    else {
                        try {
                            cs = ConnectionPool.getConnection().prepareCall("CALL unosNovogRacuna(?, ?)");
                            cs.setString(1, z.getJmb());
                            cs.setString(2, brojRacuna);
                            cs.executeUpdate();
                        } catch (SQLException e) {
                            Main.showAlertError(e.getMessage());
                        } finally {
                            ConnectionPool.close(cs);
                        }
                    }
                });
            }

        });

        obrisiButton.setOnAction(event -> {
            Zaposleni z = table.getSelectionModel().getSelectedItem();
            if (z == null)
                Main.showAlertError("Morate izabrati zaposlenog iz tabele!");
            else {
                zaposleni.remove(z);
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL obrisiZaposlenog(?)");
                    cs.setObject(1, z.getJmb());
                    cs.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs);
                }
            }
        });

        prikaziUgovorButton.setOnAction(event -> {
            Zaposleni z = table.getSelectionModel().getSelectedItem();
            if (z == null)
                Main.showAlertError("Morate izabrati zaposlenog iz tabele!");
            else {
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL getUgovorInfo(?, ?)");
                    cs.setObject(1, z.getJmb());
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.executeUpdate();
                    String text = cs.getString(2);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setGraphic(null);
                    alert.getDialogPane().getStylesheets().add("/css/main.css");
                    alert.setTitle("Ugovor");
                    alert.setHeaderText(z.getIme() + " " + z.getPrezime());
                    alert.setContentText(text);
                    alert.showAndWait();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //Main.showAlertException(e);
                } finally {
                    ConnectionPool.close(cs);
                }
            }
        });
    }

    private void pripremiZaNoviPrikaz() {
        zaposleni.clear();
    }

    private ObservableList<String> getPoslovnice() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getPoslovnice()");
            rs = cs.executeQuery();
            while (rs.next())
                list.add(rs.getString(2));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
        return list;
    }

    private void pripremiKolone(TableView<Zaposleni> tableView) {
        TableColumn[] c = tableView.getColumns().toArray(new TableColumn[8]);
        ((TableColumn<Zaposleni, Object>) c[0]).setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(column.getValue()) + 1));
        c[1].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("ime"));
        c[2].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("prezime"));
        c[3].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("adresa"));
        c[4].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("telefon"));
        c[5].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("plata"));
        c[6].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("radniOdnos"));
        c[7].setCellValueFactory(new PropertyValueFactory<Zaposleni, String>("poslovnica"));
    }
}
