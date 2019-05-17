#ifndef BANK_ICE
#define BANK_ICE

module BankingSystem {

    enum AccountType { STANDARD = 0, PREMIUM = 1};

    enum Currency {
        PLN = 0,
        USD = 1,
        AUD = 2,
        GBP = 3,
        EUR = 4,
        CHF = 5
    }


    struct Money {
        double amount;
        string currency;
    };

    struct LoanDetails {
        Money domesticValue;
        Money foreignValue;
        string loanTime;
    };

    interface Account {
        double getBalance();
    };

    interface AccountPremium extends Account {
        LoanDetails applyForLoan(Money value, string loanTime);
    };

    interface AccountStandard extends Account {};

    interface AccountFactory {
        string createAccount(string name, string pesel, Money income);
        Account* findAccount(string pesel);
    }
}
#endif
