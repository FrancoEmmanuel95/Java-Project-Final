package ui.menus.clientsmenu.clientMenuService;

import model.State;
import model.clients.Tenant;
import model.exceptions.*;
import model.genericManagement.GenericClass;
import model.genericManagement.JsonUtils;
import model.utils.Utils;
import java.util.Scanner;

import static model.utils.Utils.*;

public class TenantService {
    Scanner scanner;
    GenericClass<Tenant> tenants;

    public TenantService() {
        scanner = new Scanner(System.in);
        tenants = new GenericClass<>(JsonUtils.loadList("tenants.json", Tenant.class));
    }


    public static Tenant createTenant(Scanner scanner) throws InvalidInputException {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter surname: ");
        String surname = scanner.nextLine().trim();

        System.out.print("Enter DNI: ");
        String dni = scanner.nextLine().trim();

        System.out.print("Enter contact number: ");
        String contactNumber = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter address: ");
        String address = scanner.nextLine().trim();

        validateInputs(name, surname, contactNumber, email, address, dni);

        return new Tenant(name, surname, dni, contactNumber, email, address);
    }

    public void addTenant() {
        Boolean continueAdding;
        do {
            try {

                tenants = new GenericClass<>(JsonUtils.loadList("tenants.json", Tenant.class));
                Tenant newTenant = TenantService.createTenant(scanner);
                System.out.println("Tenant added successfully:");
                System.out.println(newTenant);
                if (!tenants.isEmpty()) {
                    Tenant t = tenants.getLastObject();
                    Integer lastId = t.getId() + 1;
                    newTenant.setId(lastId);
                }
                tenants.addElement(newTenant);
                JsonUtils.saveList(tenants.returnList(), "tenants.json", Tenant.class);
            } catch (InvalidInputException e) {
                System.out.println("Error adding tenant: " + e.getMessage());
            } catch (DuplicateElementException e) {
                System.out.println("The tenant has already exists");
            }

            continueAdding = askToContinue();
        } while (continueAdding);
    }

    private Boolean askToContinue() {
        System.out.print("Do you want to add another tenant? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    public void modifyTenant() {
        try {
            tenants = new GenericClass<>(JsonUtils.loadList("tenants.json", Tenant.class));
            if (tenants.isEmpty()) {
                System.out.println("No tenants available to modify.");
                return;
            }

            System.out.print("Enter the DNI of the tenant you want to modify: ");
            String dni = scanner.nextLine().trim();

            Tenant tenantToModify = null;
            for (Tenant tenant : tenants.returnList()) {
                if (tenant.getDni().equals(dni)) {
                    tenantToModify = tenant;
                    break;
                }
            }

            if (tenantToModify == null) {
                throw new InvalidInputException("Tenant with DNI " + dni + " not found.");
            }
            if (tenantToModify.getClientState()== State.RENTED)
            {
                throw new RentedException("You can't delete "+tenantToModify.getName()+" "+
                        tenantToModify.getSurname()+" because they have already rented a propierty");
            }

            System.out.println("Selected Tenant: " + tenantToModify);

            modifyTenantAttributes(tenantToModify);

            JsonUtils.saveList(tenants.returnList(), "tenants.json", Tenant.class);
            System.out.println("Tenant modified successfully!");
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RentedException e) {
            System.out.println("Error:  " +e.getMessage());
        }
    }

    private void modifyTenantAttributes(Tenant tenant) throws InvalidInputException {
        boolean continueModifying = true;
        while (continueModifying) {
            System.out.println("\n----------------------------------------------------");
            System.out.println("     Modify Tenant Attributes");
            System.out.println("----------------------------------------------------");
            System.out.println("1. Name");
            System.out.println("2. Surname");
            System.out.println("3. Contact Number");
            System.out.println("4. Email");
            System.out.println("5. Address");
            System.out.println("0. Go back");
            System.out.println("----------------------------------------------------");
            System.out.println("Please select the attribute you would like to modify:");

            Integer choice;
            try {
                choice = Utils.getValidatedOption();
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter new name: ");
                    String name = scanner.nextLine().trim();
                    validateName(name);
                    tenant.setName(name);
                    break;

                case 2:
                    System.out.print("Enter new surname: ");
                    String surname = scanner.nextLine().trim();
                    validateSurname(surname);
                    tenant.setSurname(surname);
                    break;

                case 3:
                    System.out.print("Enter new contact number: ");
                    String contactNumber = scanner.nextLine().trim();
                    validateContactNumber(contactNumber);
                    tenant.setContactNumber(contactNumber);
                    break;

                case 4:
                    System.out.print("Enter new email: ");
                    String email = scanner.nextLine().trim();
                    validateEmail(email);
                    tenant.setEmail(email);
                    break;

                case 5:
                    System.out.print("Enter new address: ");
                    String address = scanner.nextLine().trim();
                    validateAddress(address);
                    tenant.setAdress(address);
                    break;

                case 0:
                    continueModifying = false;
                    System.out.println("Modification process finished.");
                    break;

                default:
                    System.out.println("Invalid choice. Please select a valid option.");
                    break;
            }
        }
    }
    public void seeAllTenants() throws ElementNotFoundException {
        if (tenants.isEmpty()) {
            throw new ElementNotFoundException("No tenants found.");
        }
        System.out.println(tenants.returnList());
    }
    public void deleteTenant() {
        try {

            tenants = new GenericClass<>(JsonUtils.loadList("tenants.json", Tenant.class));
            if (tenants.isEmpty()) {
                System.out.println("No tenants available to delete.");
                return;
            }

            System.out.print("Enter the DNI of the tenant you want to delete: ");
            String dni = scanner.nextLine().trim();

            Tenant tenantToDelete = null;
            for (Tenant t : tenants.returnList()) {
                if (t.getDni().equals(dni)) {
                    tenantToDelete = t;
                    break;
                }
            }

            if (tenantToDelete == null) {
                throw new InvalidInputException("Tenant with DNI " + dni + " not found.");
            }

            if (tenantToDelete.getClientState()== State.RENTED)
            {
                throw new RentedException("You can't delete "+tenantToDelete.getName()+" "+
                        tenantToDelete.getSurname()+" because they have already rented a propierty");
            }

            System.out.println("Selected Tenant: " + tenantToDelete);

            tenants.deleteElement(tenantToDelete);

            JsonUtils.saveList(tenants.returnList(), "tenants.json", Tenant.class);
            System.out.println("Tenant deleted successfully!");
        } catch (InvalidInputException | RentedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
