package net.etfbl.bp.controllers;

import javafx.fxml.FXML;


public class MainController {
    @FXML
    private ProdajaController prodajaController;
    @FXML
    private DostupnostController dostupnostController;
    @FXML
    private StoniranjeController stoniranjeController;
    @FXML
    private NabavkaController nabavkaController;
    @FXML
    private PregledController pregledController;

    @FXML
    void initialize() {
        BaseController[] bc = {prodajaController, dostupnostController, stoniranjeController, nabavkaController, pregledController};
        for (BaseController b : bc)
            try {
                b.registerController(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public ProdajaController getProdajaController() {
        return prodajaController;
    }

    public DostupnostController getDostupnostController() {
        return dostupnostController;
    }

    public StoniranjeController getStoniranjeController() {
        return stoniranjeController;
    }

    public NabavkaController getNabavkaController() {
        return nabavkaController;
    }

    public PregledController getPregledController() {
        return pregledController;
    }
}
