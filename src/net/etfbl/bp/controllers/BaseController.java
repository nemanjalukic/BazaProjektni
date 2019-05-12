package net.etfbl.bp.controllers;


public class BaseController {
    protected MainController mainController;

    public void registerController(MainController mainController) {
        this.mainController = mainController;
    }
}
