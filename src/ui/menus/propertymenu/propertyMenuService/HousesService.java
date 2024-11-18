package ui.menus.propertymenu.propertyMenuService;

import model.clients.Owner;
import model.exceptions.DuplicateElementException;
import model.exceptions.InvalidInputException;
import model.genericManagement.GenericClass;
import model.genericManagement.JsonUtils;
import model.properties.House;
import model.properties.Property;
import model.utils.Utils;

import java.util.Scanner;

public class HousesService {
    Scanner scanner = new Scanner(System.in);
    GenericClass<Property> properties;
    GenericClass<Owner> owners;

    public HousesService() {
        scanner = new Scanner(System.in);
        properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
        owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
    }

    public void addHouse() {
        Boolean continueAdding = true;
        do {
            try {
                properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
                owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
                House newHouse = createHouse(scanner, owners);

                System.out.println("House added successfully:");
                System.out.println(newHouse);

                if (!properties.isEmpty()) {
                    Property p = properties.getLastObject();
                    Integer lastId = p.getId() + 1;
                    newHouse.setId(lastId);
                }

                properties.addElement(newHouse);
                JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);

            } catch (InvalidInputException e) {
                System.out.println("Error adding house: " + e.getMessage());
            } catch (DuplicateElementException e) {
                System.out.println("Error: " + e.getMessage());
            }
            continueAdding = askToContinue();
        } while (continueAdding);
    }

    public static House createHouse(Scanner scanner, GenericClass<Owner> ownerList) throws InvalidInputException {
        Owner owner = new Owner();
        System.out.print("Enter the owner's DNI: ");
        String ownerDni = scanner.nextLine().trim();
        owner = validateOwner(ownerDni, ownerList);

        System.out.print("Enter house address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter area: ");
        Double area = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter sales Price: ");
        Double sp = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter Rental Price: ");
        Double rp = Double.parseDouble(scanner.nextLine().trim());

        System.out.println("Enter floors quantity");
        Integer floorsQuantity = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Enter rooms quantity");
        Integer roomsQuantity = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Enter bedrooms quantity");
        Integer bedroomsQuantity = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Enter bathrooms quantity");
        Integer bathroomsQuantity = Integer.parseInt(scanner.nextLine().trim());

        Boolean park = null;
        do{
        System.out.println("The house have park? (Y/N)");
        String flag = scanner.nextLine().trim();

        if (flag.equalsIgnoreCase("Y")) {
            park = true;
        }
        else if (flag.equalsIgnoreCase("N")) {
            park = false;
        }
        }
        while(park==null);

        validateHouseInputs(address, area, sp, rp, floorsQuantity, roomsQuantity, bedroomsQuantity, bathroomsQuantity);

        return new House(owner, address, area, sp, rp, floorsQuantity, roomsQuantity, bedroomsQuantity, bathroomsQuantity, park);
    }

    public static Owner validateOwner(String ownerDni, GenericClass<Owner> ownerList) throws InvalidInputException {
        Owner owner = null;
        for (Owner ow : ownerList.returnList()) {
            if (ow.getDni().equalsIgnoreCase(ownerDni)) {
                owner = ow;
                break;
            }
        }
        if (owner == null) {
            throw new InvalidInputException("Owner with DNI " + ownerDni + " not found.");
        } else {
            return owner;
        }
    }

    public static void validateHouseInputs(String address, Double area, Double sp, Double rp, Integer floorsQuantity, Integer roomsQuantity, Integer bedroomsQuantity, Integer bathroomsQuantity) throws InvalidInputException {
        if (address.isEmpty()) {
            throw new InvalidInputException("address cannot be empty.");
        }
        if (area.isNaN()) {
            throw new InvalidInputException("area is not a number.");
        }
        if (sp.isNaN()) {
            throw new InvalidInputException("Sales price is not a number.");
        }
        if (rp.isNaN()) {
            throw new InvalidInputException("Rental price is not a number.");
        }
        if (floorsQuantity < 0) {
            throw new InvalidInputException("Floors quantity must be a positive number.");
        }
        if (roomsQuantity <= 0) {
            throw new InvalidInputException("Rooms Quantity must be a positive number.");
        }
        if (bedroomsQuantity < 0) {
            throw new InvalidInputException("Redrooms Quantity must be a positive number.");
        }
        if (bathroomsQuantity < 0) {
            throw new InvalidInputException("Bathrooms Quantity must be a positive number.");
        }
    }

    private Boolean askToContinue() {
        System.out.print("Do you want to add another house? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    public void modifyHouse() {
        Boolean continueModifying = true;
        do {
            try {
                properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
                owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));

                System.out.print("Enter the ID of the house to modify: ");
                Integer houseId = Integer.parseInt(scanner.nextLine().trim());

                House houseToModify = findHouseById(houseId);

                if (houseToModify == null) {
                    throw new InvalidInputException("House with ID " + houseId + " not found.");
                }

                System.out.println(houseToModify);

                modifyHouseDetails(houseToModify);

                JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);

                System.out.println("House modified successfully: " + houseToModify);

            } catch (InvalidInputException | NumberFormatException e) {
                System.out.println("Error modifying house: " + e.getMessage());
            }

            continueModifying = askToContinue();
        } while (continueModifying);
    }

    public House findHouseById(Integer houseId) {
        for (Property property : properties.returnList()) {
            if (property instanceof House) {
                House house = (House) property;
                if (house.getId().equals(houseId)) {
                    return house;
                }
            }
        }
        return null;
    }

    private void modifyHouseDetails(House house) throws InvalidInputException {
        Boolean continueModifying = true;
        Integer option;

        do {
            System.out.println("\n----------------------------------------------------");
            System.out.println("         Modify House Details");
            System.out.println("----------------------------------------------------");
            System.out.println("1. Address");
            System.out.println("2. Area");
            System.out.println("3. Sales Price");
            System.out.println("4. Rental Price");
            System.out.println("5. Floors Quantity");
            System.out.println("6. Rooms Quantity");
            System.out.println("7. Bedrooms Quantity");
            System.out.println("8. Bathrooms Quantity");
            System.out.println("9. Park (Yes/No)");
            System.out.println("0. Go back");
            System.out.println("----------------------------------------------------");
            System.out.println("Please select the detail you would like to modify:");

            option = Utils.getValidatedOption();

            switch (option) {
                case 1:
                    System.out.print("Address (" + house.getAdress() + "): ");
                    String newAddress = scanner.nextLine().trim();
                    if (!newAddress.isEmpty()) {
                        house.setAdress(newAddress);
                    }
                    break;

                case 2:
                    System.out.print("Area (" + house.getArea() + "): ");
                    String newArea = scanner.nextLine().trim();
                    if (!newArea.isEmpty()) {
                        Double area = Double.parseDouble(newArea);
                        validateArea(area);
                        house.setArea(area);
                    }
                    break;

                case 3:
                    System.out.print("Sales Price (" + house.getSalesPrice() + "): ");
                    String newSalesPrice = scanner.nextLine().trim();
                    if (!newSalesPrice.isEmpty()) {
                        Double sp = Double.parseDouble(newSalesPrice);
                        validatePrice(sp);
                        house.setSalesPrice(sp);
                    }
                    break;

                case 4:
                    System.out.print("Rental Price (" + house.getRentalPrice() + "): ");
                    String newRentalPrice = scanner.nextLine().trim();
                    if (!newRentalPrice.isEmpty()) {
                        Double rp = Double.parseDouble(newRentalPrice);
                        validatePrice(rp);
                        house.setRentalPrice(rp);
                    }
                    break;

                case 5:
                    System.out.print("Floors Quantity (" + house.getFloorsQuantity() + "): ");
                    String newFloorsQuantity = scanner.nextLine().trim();
                    if (!newFloorsQuantity.isEmpty()) {
                        Integer floorsQuantity = Integer.parseInt(newFloorsQuantity);
                        validateQuantity(floorsQuantity);
                        house.setFloorsQuantity(floorsQuantity);
                    }
                    break;

                case 6:
                    System.out.print("Rooms Quantity (" + house.getRooms() + "): ");
                    String newRoomsQuantity = scanner.nextLine().trim();
                    if (!newRoomsQuantity.isEmpty()) {
                        Integer roomsQuantity = Integer.parseInt(newRoomsQuantity);
                        validateQuantity(roomsQuantity);
                        house.setRooms(roomsQuantity);
                    }
                    break;

                case 7:
                    System.out.print("Bedrooms Quantity (" + house.getBedRooms() + "): ");
                    String newBedroomsQuantity = scanner.nextLine().trim();
                    if (!newBedroomsQuantity.isEmpty()) {
                        Integer bedroomsQuantity = Integer.parseInt(newBedroomsQuantity);
                        validateQuantity(bedroomsQuantity);
                        house.setBedRooms(bedroomsQuantity);
                    }
                    break;

                case 8:
                    System.out.print("Bathrooms Quantity (" + house.getBathRooms() + "): ");
                    String newBathroomsQuantity = scanner.nextLine().trim();
                    if (!newBathroomsQuantity.isEmpty()) {
                        Integer bathroomsQuantity = Integer.parseInt(newBathroomsQuantity);
                        validateQuantity(bathroomsQuantity);
                        house.setBathRooms(bathroomsQuantity);
                    }
                    break;

                case 9:
                    System.out.print("Park (" + (house.getPark() ? "Yes" : "No") + "): ");
                    String newPark = scanner.nextLine().trim();
                    if (!newPark.isEmpty()) {
                        house.setPark(newPark.equalsIgnoreCase("Y"));
                    }
                    break;

                case 0:
                    System.out.println("Returning to the previous menu...");
                    break;

                default:
                    System.out.println("Invalid option. Please choose a valid number.");
                    break;
            }

            if (option != 0) {
                System.out.print("Do you want to modify another detail? (Y/N): ");
                String continueResponse = scanner.nextLine().trim();
                continueModifying = continueResponse.equalsIgnoreCase("Y");
            } else {
                continueModifying = false;
            }

        } while (continueModifying);
    }

    public void validateArea(Double area) throws InvalidInputException {
        if (area <= 0) {
            throw new InvalidInputException("Area must be greater than zero.");
        }
    }

    public void validatePrice(Double price) throws InvalidInputException {
        if (price <= 0) {
            throw new InvalidInputException("Price must be greater than zero.");
        }
    }

    public void validateQuantity(Integer quantity) throws InvalidInputException {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }
    }


}
