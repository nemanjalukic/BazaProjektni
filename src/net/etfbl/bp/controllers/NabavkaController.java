package net.etfbl.bp.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.AutoCompleteComboBoxListener;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Proizvod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import static net.etfbl.bp.controllers.PocetnaController.ID_PRODAVNICE;

public class NabavkaController extends BaseController {

    @FXML
    private ComboBox<String> proizvodiComboBox;

    @FXML
    private ComboBox<String> dobavljacComboBox;

    @FXML
    private TableView<Proizvod> table;

    @FXML
    private Button ponistiButton;

    @FXML
    private Button obrisiButton;

    @FXML
    private Button gotovoButton;

    @FXML
    private Button unosPodatakaButton;

    private VBox vBox;

    private ObservableList<String> proizvodi, dobavljaci, atributiProizvoda, proizvodjaci;
    private ObservableList<Proizvod> tableData;
    private PreparedStatement ps;
    private CallableStatement cs;
    private Statement s;
    private ResultSet rs;

    @FXML
    void initialize() {
        dobavljaci = FXCollections.observableArrayList();
        proizvodjaci = FXCollections.observableArrayList();
        dobavljacComboBox.setItems(dobavljaci);
        new AutoCompleteComboBoxListener(dobavljacComboBox);
        tableData = FXCollections.observableArrayList();
        table.setItems(tableData);
        pripremiKolone(table);

        try {
            ps = ConnectionPool.getConnection().prepareStatement("SELECT Naziv FROM proizvodjac");
            rs = ps.executeQuery();
            while (rs.next()) {
                proizvodjaci.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(ps, rs);
        }
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getDobavljace()");
            rs = cs.executeQuery();
            while (rs.next()) {
                dobavljaci.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(s, rs);
        }

        proizvodi = FXCollections.observableArrayList();
        proizvodi.addAll("Ostalo", "Fasada", "Enterijer", "Auto", "Farba", "Plocice");
        atributiProizvoda = FXCollections.observableArrayList();
        atributiProizvoda.addAll("Naziv", "Opis", "Kolicina", "Nabavna Cijena", "Prodajna Cijena");

        proizvodiComboBox.setItems(proizvodi);

        unosPodatakaButton.setOnAction(event -> {
            if (proizvodiComboBox.getSelectionModel().getSelectedIndex() == -1) {
                Main.showAlertError("Morate izabrati proizvod!");
            } else {
                ObservableList<String> atr = FXCollections.observableArrayList();
                String tip = proizvodiComboBox.getValue();
                atr.addAll(atributiProizvoda);

                prikaziDijalogZaUnos(atr, tip);
            }

        });

        obrisiButton.setOnAction(event -> {
            Proizvod a = table.getSelectionModel().getSelectedItem();
            if (a != null) {
                tableData.remove(a);
            } else {
                Main.showAlertError("Morate izabrati proizvod iz tabele!");
            }
        });

        ponistiButton.setOnAction(event -> {
            dobavljacComboBox.getEditor().clear();
            tableData.clear();
        });

        gotovoButton.setOnAction(event -> {
            if (dobavljacComboBox.getEditor().getText().isEmpty()) {
                Main.showAlertError("Morate unijeti dobavljača!");
            } else if (tableData.isEmpty()) {
                Main.showAlertWarining("Morate unijeti stavke nabavke!");
            } else {
                dodajUBazu();
            }
        });
    }

    private void dodajUBazu() {
        CallableStatement cs1 = null;
        PreparedStatement ps = null, ps1 = null;
        try {
            // get ID dobavljaca 
            cs = ConnectionPool.getConnection().prepareCall("CALL provjeriDobavljaca(?, ?)");
            cs.setObject(1, dobavljacComboBox.getValue());
            cs.registerOutParameter(2, Types.INTEGER);
            cs.executeUpdate();
            Integer idDobavljaca = cs.getInt(2);
            // unesi nabavku,
            cs = ConnectionPool.getConnection().prepareCall("CALL unosNoveNabavke(?, ?, ?)");
            cs.setObject(1, ID_PRODAVNICE);
            cs.setObject(2, idDobavljaca);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.executeUpdate();
            Integer brojFakture = cs.getInt(3);
            // unos stavki nabavke

            cs1 = ConnectionPool.getConnection().prepareCall("CALL provjeriArtikl(?, ?, ?, ?)");
            ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO artikl (Naziv, Cijena, Opis, IdProizvodjaca) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps1 = ConnectionPool.getConnection().prepareStatement("INSERT INTO stavka_nabavke(Sifra,BrojFakture,NabavnaCijena,Kolicina) VALUES (?, ?, ?, ?)");
            s = ConnectionPool.getConnection().createStatement();
            for (Proizvod p : tableData) {
                // id dobavljaca
                cs1.setString(1, p.getProizvodjac());
                cs1.setString(2, p.getNaziv());
                cs1.registerOutParameter(3, Types.INTEGER);
                cs1.registerOutParameter(4, Types.INTEGER);
                cs1.executeUpdate();
                Integer idProizvodjaca = cs1.getInt(3);
                Integer sifra = cs1.getInt(4);
                if (sifra == 0) {
                    // unos u proizvod tabelu
                    ps.setObject(1, p.getNaziv());
                    ps.setObject(2, p.getProdajnacijena());
                    ps.setObject(3, p.getOpis());
                    ps.setObject(4, idProizvodjaca);
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    rs.next();
                    sifra = rs.getInt(1);
                    if (p.getProizvod().equals("Fasada")) {
                        cs = ConnectionPool.getConnection().prepareCall("CALL unosFasada(?, ?, ?, ?)");
                        cs.setObject(1, sifra);
                        cs.setObject(2, p.getBoja());
                        cs.setObject(3, p.getPakovanje());
                        cs.setObject(4, p.getPotrosnja());
                        cs.executeUpdate();
                    }
                    if (p.getProizvod().equals("Enterijer")) {
                        cs = ConnectionPool.getConnection().prepareCall("CALL unosEnterijer(?, ?, ?, ?, ?)");
                        cs.setObject(1, sifra);
                        cs.setObject(2, p.getBoja());
                        cs.setObject(3, p.getPakovanje());
                        cs.setObject(4, p.getPotrosnja());
                        cs.setObject(5, p.getRokUpotrebe());
                        cs.executeUpdate();

                        for (String na : p.getNanosenje()) {
                            cs = ConnectionPool.getConnection().prepareCall("CALL unosNanosenja(?, ?)");
                            cs.setObject(1, na);
                            cs.setObject(2, sifra);
                            cs.executeUpdate();
                        }

                    }
                    if (p.getProizvod().equals("Farba")) {
                        cs = ConnectionPool.getConnection().prepareCall("CALL unosFarba(?, ?, ?, ?, ?)");
                        cs.setObject(1, sifra);
                        cs.setObject(2, p.getTip());
                        cs.setObject(3, p.getBoja());
                        cs.setObject(4, p.getPakovanje());
                        cs.setObject(5, p.getPotrosnja());
                        cs.executeUpdate();

                        for (String na : p.getNanosenje()) {
                            cs = ConnectionPool.getConnection().prepareCall("CALL unosNanosenja(?, ?)");
                            cs.setObject(1, na);
                            cs.setObject(2, sifra);
                            cs.executeUpdate();
                        }

                    }

                    if (p.getProizvod().equals("Auto")) {
                        cs = ConnectionPool.getConnection().prepareCall("CALL unosAuto(?, ?, ?, ?)");
                        cs.setObject(1, sifra);
                        cs.setObject(2, p.getBoja());
                        cs.setObject(3, p.getPakovanje());
                        cs.setObject(4, p.getPotrosnja());
                        cs.executeUpdate();

                        for (String na : p.getNanosenje()) {
                            cs = ConnectionPool.getConnection().prepareCall("CALL unosNanosenja(?, ?)");
                            cs.setObject(1, na);
                            cs.setObject(2, sifra);
                            cs.executeUpdate();
                        }

                    }
                    if (p.getProizvod().equals("Plocice")) {
                        cs = ConnectionPool.getConnection().prepareCall("CALL unosPlocice(?, ?, ?)");
                        cs.setObject(1, sifra);
                        cs.setObject(2, p.getTip());
                        cs.setObject(3, p.getDimenzije());
                        cs.executeUpdate();

                    }

                    mainController.getProdajaController().getSifraData().add(sifra.toString());
                    mainController.getDostupnostController().getSifraData().add(sifra.toString());
                }

                // unos u stavku nabavke
                ps1.setObject(1, sifra);
                ps1.setObject(2, brojFakture);
                ps1.setObject(3, p.getNabavnacijena());
                ps1.setObject(4, p.getKolicina());
                ps1.executeUpdate();

               
            }
            tableData.clear();
        } catch (Exception e) {
            e.printStackTrace();
            Main.showAlertException(e);
        } finally {
            ConnectionPool.close(rs, cs, cs1, ps, ps1, s);
        }
    }

    private void prikaziDijalogZaUnos(List<String> atributi, String proizvod) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unos podataka");
        alert.setHeaderText("Unos podataka za: " + " " + proizvod + ".");
        alert.setGraphic(null);
        vBox = new VBox();
        vBox.setSpacing(10);
        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(400);
        vBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(35));
        alert.getDialogPane().setContent(scrollPane);

        ComboBox<String> comboBox = new ComboBox<>(proizvodjaci);
        comboBox.setPromptText("Proizvodjaci");
        comboBox.setPrefHeight(30);
        comboBox.setPrefWidth(360);
        comboBox.setStyle("-fx-font-size: 13px;");
        vBox.getChildren().add(comboBox);

        for (String s : atributi) {
            TextField tf = new TextField();
            tf.setPromptText(s);
            tf.setPrefHeight(30);
            tf.setFont(new Font(13));
            vBox.getChildren().add(tf);
        }

        if (proizvod.equals("Fasada")) {
            ObservableList<String> atri;
            atri = FXCollections.observableArrayList();
            atri.addAll("Boja", "Potrosnja", "Pakovanje");
            for (String s : atri) {
                TextField tf = new TextField();
                tf.setPromptText(s);
                tf.setPrefHeight(30);
                tf.setFont(new Font(13));
                vBox.getChildren().add(tf);
            }

        }
        if (proizvod.equals("Enterijer")) {
            ObservableList<String> atri;
            atri = FXCollections.observableArrayList();
            atri.addAll("Boja", "Potrosnja", "Pakovanje");

            ObservableList<String> nanosenje;
            nanosenje = FXCollections.observableArrayList();
            nanosenje.addAll("Cetka", "Valjak", "Sprica");

            for (String s : atri) {
                TextField tf = new TextField();
                tf.setPromptText(s);
                tf.setPrefHeight(30);
                tf.setFont(new Font(13));
                vBox.getChildren().add(tf);
            }
            DatePicker dp1 = new DatePicker();
            dp1.setPromptText("Datum upotrebe");
            dp1.setPrefWidth(360);
            dp1.setPrefHeight(30);
            dp1.getEditor().setFont(new Font(13));
            vBox.getChildren().add(dp1);

            MenuButton mb = new MenuButton();
            mb.setText("Nacini nanosenja");
            mb.setPrefHeight(30);
            mb.setPrefWidth(360);
            mb.setStyle("-fx-font-size: 13px;");
            for (String s1 : nanosenje) {
                mb.getItems().add(new CheckMenuItem(s1));
            }

            vBox.getChildren().add(mb);

        }
        if (proizvod.equals("Auto")) {
            ObservableList<String> atri;
            atri = FXCollections.observableArrayList();
            atri.addAll("Boja", "Potrosnja", "Pakovanje");

            ObservableList<String> nanosenje;
            nanosenje = FXCollections.observableArrayList();
            nanosenje.addAll("Cetka", "Valjak", "Sprica");

            for (String s : atri) {
                TextField tf = new TextField();
                tf.setPromptText(s);
                tf.setPrefHeight(30);
                tf.setFont(new Font(13));
                vBox.getChildren().add(tf);
            }

            MenuButton mb = new MenuButton();
            mb.setText("Nacini nanosenja");
            mb.setPrefHeight(30);
            mb.setPrefWidth(360);
            mb.setStyle("-fx-font-size: 13px;");
            for (String s1 : nanosenje) {
                mb.getItems().add(new CheckMenuItem(s1));
            }

            vBox.getChildren().add(mb);

        }
        if (proizvod.equals("Farba")) {
            ObservableList<String> atri;
            atri = FXCollections.observableArrayList();
            atri.addAll("Boja", "Potrosnja", "Pakovanje");

            ObservableList<String> tip;
            tip = FXCollections.observableArrayList();
            tip.addAll("Drvo", "Metal", "Beton");

            ObservableList<String> nanosenje;
            nanosenje = FXCollections.observableArrayList();
            nanosenje.addAll("Cetka", "Valjak", "Sprica");
            for (String s : atri) {
                TextField tf = new TextField();
                tf.setPromptText(s);
                tf.setPrefHeight(30);
                tf.setFont(new Font(13));
                vBox.getChildren().add(tf);
            }

            ComboBox<String> comboBox1 = new ComboBox<>(tip);
            comboBox1.setPromptText("Tip farbe");
            comboBox1.setPrefHeight(30);
            comboBox1.setPrefWidth(360);
            comboBox1.setStyle("-fx-font-size: 13px;");
            vBox.getChildren().add(comboBox1);

            MenuButton mb = new MenuButton();
            mb.setText("Nacini nanosenja");
            mb.setPrefHeight(30);
            mb.setPrefWidth(360);
            mb.setStyle("-fx-font-size: 13px;");
            for (String s1 : nanosenje) {
                mb.getItems().add(new CheckMenuItem(s1));
            }

            vBox.getChildren().add(mb);
        }
        if (proizvod.equals("Plocice")) {
            ObservableList<String> tip;
            tip = FXCollections.observableArrayList();
            tip.addAll("Vanjske", "Unutrasnje");
            TextField tf = new TextField();
            tf.setPromptText("Dimenzije");
            tf.setPrefHeight(30);
            tf.setFont(new Font(13));
            vBox.getChildren().add(tf);

            ComboBox<String> comboBox1 = new ComboBox<>(tip);
            comboBox1.setPromptText("Tip plocica");
            comboBox1.setPrefHeight(30);
            comboBox1.setPrefWidth(360);
            comboBox1.setStyle("-fx-font-size: 13px;");
            vBox.getChildren().add(comboBox1);
        }

        if (alert.showAndWait().get().getButtonData().isDefaultButton()) {
            if (proizvod.equals("Ostalo")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                tableData.add(pro);
            }
            if (proizvod.equals("Fasada")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                pro.setBoja(((TextField) l.get(6)).getText());
                pro.setPotrosnja(((TextField) l.get(7)).getText());
                pro.setPakovanje(((TextField) l.get(8)).getText());
                tableData.add(pro);
            }
            if (proizvod.equals("Enterijer")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                pro.setBoja(((TextField) l.get(6)).getText());
                pro.setPotrosnja(((TextField) l.get(7)).getText());
                pro.setPakovanje(((TextField) l.get(8)).getText());
                pro.setRokUpotrebe(((DatePicker) l.get(9)).getValue().toString());
                pro.setNanosenje(vratiNanosenje((MenuButton) l.get(10)));
                tableData.add(pro);
            }
            if (proizvod.equals("Auto")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                pro.setBoja(((TextField) l.get(6)).getText());
                pro.setPotrosnja(((TextField) l.get(7)).getText());
                pro.setPakovanje(((TextField) l.get(8)).getText());
                pro.setNanosenje(vratiNanosenje((MenuButton) l.get(9)));
                tableData.add(pro);
            }
            if (proizvod.equals("Farba")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                pro.setBoja(((TextField) l.get(6)).getText());
                pro.setPotrosnja(((TextField) l.get(7)).getText());
                pro.setPakovanje(((TextField) l.get(8)).getText());
                pro.setTip(((ComboBox<String>) l.get(9)).getValue());
                pro.setNanosenje(vratiNanosenje((MenuButton) l.get(10)));
                tableData.add(pro);
            }
            if (proizvod.equals("Plocice")) {
                List<Node> l = vBox.getChildren();
                Proizvod pro = new Proizvod(
                        ((ComboBox<String>) l.get(0)).getValue(),
                        ((TextField) l.get(1)).getText(),
                        ((TextField) l.get(2)).getText(),
                        Integer.valueOf(((TextField) l.get(3)).getText()),
                        ((TextField) l.get(4)).getText(),
                        ((TextField) l.get(5)).getText());
                pro.setProizvod(proizvod);
                pro.setDimenzije(((TextField) l.get(6)).getText());
                pro.setTip(((ComboBox<String>) l.get(7)).getValue());
                tableData.add(pro);
            }

        }
    }

    private ArrayList<String> vratiNanosenje(MenuButton mb) {
        ArrayList<String> pom = new ArrayList<>();
        for (MenuItem item : mb.getItems()) {
            CheckMenuItem checkMenuItem = (CheckMenuItem) item;
            if (checkMenuItem.isSelected()) {
                pom.add(checkMenuItem.getText());
            }
        }

        return pom;
    }

    private void pripremiKolone(TableView<Proizvod> tableView) {
        TableColumn[] c = tableView.getColumns().toArray(new TableColumn[6]);
        ((TableColumn<Proizvod, Number>) c[0]).setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(column.getValue()) + 1));
        c[1].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("naziv"));
        c[2].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("proizvodjac"));
        c[3].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("nabavnacijena"));
        c[4].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("prodajnacijena"));
        c[5].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("kolicina"));
    }
}
