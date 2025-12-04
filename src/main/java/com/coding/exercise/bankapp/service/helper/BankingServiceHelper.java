package com.coding.exercise.bankapp.service.helper;

import com.coding.exercise.bankapp.domain.*;
import com.coding.exercise.bankapp.model.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Helper class encapsulating mapping utilities between entities and DTOs, and BigDecimal operations.
 * This mirrors conventions from the source application (BankApp-179898).
 */
// PUBLIC_INTERFACE
public final class BankingServiceHelper {

    private BankingServiceHelper() {
        // utility
    }

    // PUBLIC_INTERFACE
    public static Customer toCustomerEntity(CustomerDetails details) {
        if (details == null) return null;
        Customer.CustomerBuilder builder = Customer.builder()
                .id(details.getId())
                .firstName(details.getFirstName())
                .lastName(details.getLastName())
                .dateOfBirth(details.getDateOfBirth());

        if (details.getAddress() != null) {
            builder.address(toAddressEntity(details.getAddress()));
        }
        if (details.getContact() != null) {
            builder.contact(toContactEntity(details.getContact()));
        }
        return builder.build();
    }

    // PUBLIC_INTERFACE
    public static CustomerDetails toCustomerDetails(Customer entity) {
        if (entity == null) return null;
        return CustomerDetails.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .address(toAddressDetails(entity.getAddress()))
                .contact(toContactDetails(entity.getContact()))
                .build();
    }

    private static Address toAddressEntity(AddressDetails details) {
        if (details == null) return null;
        return Address.builder()
                .line1(details.getLine1())
                .line2(details.getLine2())
                .city(details.getCity())
                .state(details.getState())
                .zip(details.getZip())
                .country(details.getCountry())
                .build();
    }

    private static AddressDetails toAddressDetails(Address entity) {
        if (entity == null) return null;
        return AddressDetails.builder()
                .line1(entity.getLine1())
                .line2(entity.getLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .zip(entity.getZip())
                .country(entity.getCountry())
                .build();
    }

    private static Contact toContactEntity(ContactDetails details) {
        if (details == null) return null;
        return Contact.builder()
                .phone(details.getPhone())
                .email(details.getEmail())
                .build();
    }

    private static ContactDetails toContactDetails(Contact entity) {
        if (entity == null) return null;
        return ContactDetails.builder()
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build();
    }

    // PUBLIC_INTERFACE
    public static Account toAccountEntity(AccountInformation info, BankInfo bankInfo) {
        if (info == null) return null;
        return Account.builder()
                .id(info.getId())
                .accountNumber(info.getAccountNumber())
                .type(info.getType())
                .currency(info.getCurrency())
                .bankInfo(bankInfo)
                .balance(BigDecimal.ZERO) // initial default; service will set/maintain balance
                .build();
    }

    // PUBLIC_INTERFACE
    public static AccountInformation toAccountInformation(Account entity) {
        if (entity == null) return null;
        String bankCode = null;
        String bankName = null;
        if (entity.getBankInfo() != null) {
            bankCode = entity.getBankInfo().getBankCode();
            bankName = entity.getBankInfo().getBankName();
        }
        return AccountInformation.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .type(entity.getType())
                .currency(entity.getCurrency())
                .bankCode(bankCode)
                .bankName(bankName)
                .build();
    }

    // PUBLIC_INTERFACE
    public static BankInfo toBankInfoEntity(BankInformation info) {
        if (info == null) return null;
        return BankInfo.builder()
                .bankCode(info.getBankCode())
                .bankName(info.getBankName())
                .build();
    }

    // PUBLIC_INTERFACE
    public static BankInformation toBankInformation(BankInfo entity) {
        if (entity == null) return null;
        return BankInformation.builder()
                .bankCode(entity.getBankCode())
                .bankName(entity.getBankName())
                .build();
    }

    // PUBLIC_INTERFACE
    public static TransactionDetails toTransactionDetails(Transaction entity) {
        if (entity == null) return null;
        Long accId = entity.getAccount() == null ? null : entity.getAccount().getId();
        return TransactionDetails.builder()
                .id(entity.getId())
                .accountId(accId)
                .amount(scaled(entity.getAmount()))
                .type(entity.getType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // PUBLIC_INTERFACE
    public static BigDecimal scaled(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
        // Source application uses currency-like scaling; HALF_UP matches common monetary rounding.
    }

    // PUBLIC_INTERFACE
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    // PUBLIC_INTERFACE
    public static void applyNonNullCustomerUpdates(Customer target, CustomerDetails updates) {
        if (target == null || updates == null) return;
        if (Objects.nonNull(updates.getFirstName())) target.setFirstName(updates.getFirstName());
        if (Objects.nonNull(updates.getLastName())) target.setLastName(updates.getLastName());
        if (Objects.nonNull(updates.getDateOfBirth())) target.setDateOfBirth(updates.getDateOfBirth());
        if (Objects.nonNull(updates.getAddress())) target.setAddress(toAddressEntity(updates.getAddress()));
        if (Objects.nonNull(updates.getContact())) target.setContact(toContactEntity(updates.getContact()));
    }

    // PUBLIC_INTERFACE
    public static void applyNonNullAccountUpdates(Account target, AccountInformation updates, BankInfo bankInfo) {
        if (target == null || updates == null) return;
        if (Objects.nonNull(updates.getAccountNumber())) target.setAccountNumber(updates.getAccountNumber());
        if (Objects.nonNull(updates.getType())) target.setType(updates.getType());
        if (Objects.nonNull(updates.getCurrency())) target.setCurrency(updates.getCurrency());
        if (bankInfo != null) target.setBankInfo(bankInfo);
        // Note: balance is not updated here; only via monetary operations
    }
}
