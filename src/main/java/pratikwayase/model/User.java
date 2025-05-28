package pratikwayase.model;



import pratikwayase.enums.AccountStatus;
import pratikwayase.enums.AccountType;

// Introduced a base User class to unify Guest, Receptionist, etc.
public abstract class User {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private AccountStatus accountStatus;
    private final AccountType accountType;

    public User(String id, String name, String email, String phone, AccountType accountType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.accountStatus = AccountStatus.ACTIVE; // Default status
        this.accountType = accountType;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public AccountType getAccountType() { return accountType; }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    // Abstract method to be implemented by subclasses for specific display logic
    public abstract void displayInfo();
}