package ui.menus.salesmenu.saleMenuService;

import model.State;
import model.clients.Buyer;
import model.clients.Owner;
import model.exceptions.DuplicateElementException;
import model.exceptions.ElementNotFoundException;
import model.exceptions.InvalidInputException;
import model.genericManagement.GenericClass;
import model.genericManagement.JsonUtils;
import model.properties.Property;
import model.sales.Sale;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class SaleService {
    private GenericClass<Property> properties;
    private GenericClass<Sale> sales;
    private GenericClass<Buyer> buyers;
    private GenericClass<Owner> owners;
    private Scanner scanner;

    public SaleService() {
        sales = new GenericClass<>(JsonUtils.loadList("sales.json", Sale.class));
        buyers = new GenericClass<>(JsonUtils.loadList("buyers.json", Buyer.class));
        properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
        owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
        scanner = new Scanner(System.in);
    }

    public void addSale() {
        Boolean continueAdding;
        do {
            try {
                sales = new GenericClass<>(JsonUtils.loadList("sales.json", Sale.class));
                buyers = new GenericClass<>(JsonUtils.loadList("buyers.json", Buyer.class));
                properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
                owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
                Sale newSale = createSale();

                System.out.println("Sale created successfully!");
                System.out.println(newSale);

                if (!sales.isEmpty()) {
                    Sale s = sales.getLastObject();
                    Integer lastId = s.getId() + 1;
                    newSale.setId(lastId);
                }

                sales.addElement(newSale);

                JsonUtils.saveList(sales.returnList(), "sales.json", Sale.class);

            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println(("Error:  " + e.getMessage()));
            } catch (DuplicateElementException e) {
                System.out.println("Error:   " + e.getMessage());
            }

            continueAdding = askToContinue();

        } while (continueAdding);
    }


    public Sale createSale() throws InvalidInputException {

        Integer propertyId;
        try
        {
            System.out.print("Enter the ID of the property to sell: ");
            propertyId = Integer.parseInt(scanner.nextLine().trim());
        }catch (NumberFormatException e) {
            throw new InvalidInputException("The Id must be a valid number.");
        }


        Property property = findPropertyById(propertyId);

        if (property == null || property.getState() != State.AVAILABLE) {
            throw new InvalidInputException("Property is not available for sale."); //VER DE MANEJAR OTRA EXCEP
        }

        System.out.print("Enter the buyer's DNI: ");
        String buyerDni = scanner.nextLine().trim();
        Buyer buyer = validateBuyer(buyerDni);

        System.out.print("Enter the sale date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        LocalDate saleDate = dateValidation(LocalDate.parse(date));


        //SETTING THE CLIENTS STATE
        //SETTING THE BUYER STATE
        buyer.setClientState(State.BOUGHT);
        buyers.modifyElement(buyer, buyer);
        JsonUtils.saveList(buyers.returnList(), "buyers.json", Buyer.class);
        //SETTING THE OWNER STATE
        property.getOwner().setClientState(State.SOLD);
        owners.modifyElement(property.getOwner(), property.getOwner());
        JsonUtils.saveList(owners.returnList(), "owners.json", Owner.class);

        //SETTING THE PROPERTY STATE
        property.setState(State.SOLD);
        properties.modifyElement(property, property);
        JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);

        return new Sale(buyer, property, saleDate);
    }


    private Property findPropertyById(Integer propertyId) {
        for (Property p : properties.returnList()) {
            if (p.getId().equals(propertyId)) {
                return p;
            }
        }
        return null;
    }

    private Buyer validateBuyer(String buyerDni) throws InvalidInputException {
        for (Buyer b : buyers.returnList()) {
            if (b.getDni().equalsIgnoreCase(buyerDni)) {
                return b;
            }
        }
        throw new InvalidInputException("Buyer with DNI " + buyerDni + " not found.");
    }

    private Boolean askToContinue() {
        System.out.print("Do you want to add another sale? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    public LocalDate dateValidation(LocalDate startDate) throws InvalidInputException {
        if (startDate == null) {
            throw new InvalidInputException("The date cannot be null");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidInputException("The sale date cannot be earlier than today");
        }
        return startDate;
    }

    public void seeAllSales() throws ElementNotFoundException {
        if (sales.isEmpty()) {
            throw new ElementNotFoundException("No sales found.");
        }
        System.out.println(sales.returnList());
    }

    public Sale findSaleById(Integer rentId) {
        for (Sale sale : sales.returnList()) {
            if (sale instanceof Sale) {
                Sale sale1 = (Sale) sale;
                if (sale1.getId().equals(rentId)) {
                    return sale1;
                }
            }
        }
        return null;
    }


    public void validateArea(Double area) throws InvalidInputException {
        if (area <= 0) {
            throw new InvalidInputException("Area must be greater than zero.");
        }
    }

    public void deleteSale() {
        try {

            buyers = new GenericClass<>(JsonUtils.loadList("buyers.json", Buyer.class));
            properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
            sales = new GenericClass<>(JsonUtils.loadList("sales.json", Sale.class));
            if (sales.isEmpty()) {
                System.out.println("No sales available to delete.");
                return;
            }

            System.out.print("Enter the ID of the sale you want to delete: ");
            Integer saleID = Integer.parseInt(scanner.nextLine().trim());

            Buyer buyer = null;
            Property property = null;
            Sale saleToDelete = null;
            for (Sale s : sales.returnList()) {
                if (s.getId().equals(saleID)) {
                    saleToDelete = s;
                    buyer = s.getBuyer();
                    property = s.getProperty();
                    break;
                }
            }

            if (saleToDelete == null) {
                throw new InvalidInputException("Sale with ID " + saleID + " not found.");
            }


            System.out.println("Selected sale: " + saleToDelete);

            sales.deleteElement(saleToDelete);
            //MODIFY STATE OF PROPIERTY AND STATE OF CLIENT
            buyer.setClientState(State.AVAILABLE);
            property.setState(State.AVAILABLE);

            buyers.modifyElement(buyer, buyer);
            properties.modifyElement(property, property);


            JsonUtils.saveList(buyers.returnList(), "buyers.json", Buyer.class);
            JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);
            JsonUtils.saveList(sales.returnList(), "sales.json", Sale.class);

            System.out.println("Sale deleted successfully!");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }





}