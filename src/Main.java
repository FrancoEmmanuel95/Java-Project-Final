import model.clients.Buyer;
import model.clients.Owner;

import model.clients.Tenant;
import model.genericManagement.JsonUtils;
import model.properties.*;
import model.rents.Rent;
import model.sales.Sale;
import ui.menus.mainMenu.MainMenu;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        MainMenu mm = new MainMenu();
        mm.menu();

    }
}