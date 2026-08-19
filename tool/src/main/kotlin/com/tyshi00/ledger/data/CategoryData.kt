package com.tyshi00.ledger.data

enum class EntryType(val label: String) {
    INCOME("Income"),
    FIXED("Fixed Expenses"),
    VARIABLE("Variable Expenses"),
    DEBT("Debts"),
}

enum class Frequency(val label: String) {
    ONE_TIME("One-time"),
    WEEKLY("Weekly"),
    BIWEEKLY("Biweekly"),
    BIMONTHLY("Bimonthly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    ANNUALLY("Annually"),
}

enum class CategoryGroup(val label: String, val type: EntryType) {
    INCOME("Income", EntryType.INCOME),
    HOUSING("Housing", EntryType.FIXED),
    SAVINGS_INVESTMENTS("Savings & Investments", EntryType.FIXED),
    FOOD_HOUSEHOLD("Food / Household", EntryType.FIXED),
    MEDICAL("Medical", EntryType.FIXED),
    INSURANCE("Insurance", EntryType.FIXED),
    TRANSPORTATION("Transportation", EntryType.FIXED),
    FIXED_MISC("Miscellaneous", EntryType.FIXED),
    PERSONAL_CARE("Personal Care", EntryType.VARIABLE),
    ENTERTAINMENT("Entertainment", EntryType.VARIABLE),
    VARIABLE_MISC("Miscellaneous", EntryType.VARIABLE),
    DEBTS("Debts", EntryType.DEBT),
}

enum class DefaultCategory(val label: String, val group: CategoryGroup) {
    // Income
    EMPLOYER("Income / Employer", CategoryGroup.INCOME),
    PART_TIME("Part-time / Second Job", CategoryGroup.INCOME),
    FAMILY_SUPPORT("Support from Family/Friends", CategoryGroup.INCOME),
    RETIREMENT_PENSION("Retirement / Pension", CategoryGroup.INCOME),
    CHILD_SUPPORT_INCOME("Child Support / Alimony", CategoryGroup.INCOME),
    SOCIAL_SECURITY("Social Security", CategoryGroup.INCOME),
    GOV_ASSISTANCE("Government Assistance", CategoryGroup.INCOME),
    UNEMPLOYMENT("Unemployment", CategoryGroup.INCOME),
    RENTAL_INCOME("Rental Income", CategoryGroup.INCOME),

    // Housing
    RENT("Rent", CategoryGroup.HOUSING),
    MORTGAGE("Mortgage", CategoryGroup.HOUSING),
    HOME_INSURANCE("Homeowner's / Renter's Insurance", CategoryGroup.HOUSING),
    PROPERTY_TAXES("Property Taxes", CategoryGroup.HOUSING),
    HOA_FEES("HOA / Renter Fees", CategoryGroup.HOUSING),
    UTILITIES_ENERGY("Gas / Electric / Oil", CategoryGroup.HOUSING),
    UTILITIES_WATER("Water / Sewer / Garbage", CategoryGroup.HOUSING),
    PHONE("Phone / Cellphone", CategoryGroup.HOUSING),

    // Savings & Investments
    SAVINGS_ACCOUNT("Savings for a Goal", CategoryGroup.SAVINGS_INVESTMENTS),
    EMERGENCY_SAVINGS("Emergency Savings", CategoryGroup.SAVINGS_INVESTMENTS),
    FOUR_OH_ONE_K("401k / 403B", CategoryGroup.SAVINGS_INVESTMENTS),
    IRA_ACCOUNT("IRA Account", CategoryGroup.SAVINGS_INVESTMENTS),
    STOCKS("Stocks", CategoryGroup.SAVINGS_INVESTMENTS),

    // Food / Household
    GROCERIES("Groceries & Household Items", CategoryGroup.FOOD_HOUSEHOLD),
    LUNCHES("Lunches at Work / School", CategoryGroup.FOOD_HOUSEHOLD),
    DINING_OUT("Dining Out", CategoryGroup.FOOD_HOUSEHOLD),

    // Medical
    PRIMARY_SPECIALIST("Primary / Specialist", CategoryGroup.MEDICAL),
    VISION_GLASSES("Vision / Glasses", CategoryGroup.MEDICAL),
    DENTAL("Dental", CategoryGroup.MEDICAL),
    PRESCRIPTIONS("Prescriptions", CategoryGroup.MEDICAL),

    // Insurance
    HEALTH_INSURANCE("Health / Dental / Vision Insurance", CategoryGroup.INSURANCE),
    LIFE_DISABILITY("Life / Disability Insurance", CategoryGroup.INSURANCE),
    AUTO_INSURANCE("Auto Insurance", CategoryGroup.INSURANCE),

    // Transportation
    VEHICLE_PAYMENT_1("Vehicle Payment #1", CategoryGroup.TRANSPORTATION),
    VEHICLE_PAYMENT_2("Vehicle Payment #2", CategoryGroup.TRANSPORTATION),
    REGISTRATION("Registration", CategoryGroup.TRANSPORTATION),
    GAS("Gas", CategoryGroup.TRANSPORTATION),
    MAINTENANCE("Maintenance", CategoryGroup.TRANSPORTATION),
    PUBLIC_TRANSIT("Public Transit / Tolls / Parking", CategoryGroup.TRANSPORTATION),

    // Fixed Misc
    CHILD_CARE("Child Care", CategoryGroup.FIXED_MISC),
    CHILD_SUPPORT_EXPENSE("Child Support / Alimony", CategoryGroup.FIXED_MISC),
    PET_CARE("Pet Care", CategoryGroup.FIXED_MISC),
    STORAGE_FEES("Storage Fees", CategoryGroup.FIXED_MISC),
    TAX_REPAYMENT("Fed / State Tax Repayment", CategoryGroup.FIXED_MISC),
    STUDENT_LOANS("Student Loans", CategoryGroup.FIXED_MISC),

    // Personal Care
    HAIRCARE("Haircare", CategoryGroup.PERSONAL_CARE),
    CLOTHING("Clothing Expenses", CategoryGroup.PERSONAL_CARE),
    COSMETICS_SPA("Cosmetics / Skincare / Spa", CategoryGroup.PERSONAL_CARE),
    LAUNDRY("Misc. Laundry Expenses", CategoryGroup.PERSONAL_CARE),

    // Entertainment
    CABLE_STREAMING("Cable / Streaming", CategoryGroup.ENTERTAINMENT),
    INTERNET("Internet Service", CategoryGroup.ENTERTAINMENT),
    MUSIC_SERVICES("Music / Audio Services", CategoryGroup.ENTERTAINMENT),
    APP_SUBSCRIPTIONS("App Subscriptions", CategoryGroup.ENTERTAINMENT),
    LOCAL_OUTINGS("Local Outings", CategoryGroup.ENTERTAINMENT),
    TRAVEL_VACATION("Travel / Vacation", CategoryGroup.ENTERTAINMENT),
    SPORTS_MEMBERSHIPS("Sports / Memberships", CategoryGroup.ENTERTAINMENT),

    // Variable Misc
    TUITION("Tuition / Continuing Ed", CategoryGroup.VARIABLE_MISC),
    LAWN_POOL("Pool / Lawn Care", CategoryGroup.VARIABLE_MISC),
    BANKING_FEES("Banking Fees", CategoryGroup.VARIABLE_MISC),
    DONATIONS("Donations", CategoryGroup.VARIABLE_MISC),
    GIFTS("Holiday / Birthday Gifts", CategoryGroup.VARIABLE_MISC),
    RECREATIONAL("Recreational Expenses", CategoryGroup.VARIABLE_MISC),
}

data class CategoryItem(
    val id: String,
    val label: String,
    val group: CategoryGroup,
    val isCustom: Boolean,
)

fun DefaultCategory.toItem() = CategoryItem(
    id = name,
    label = label,
    group = group,
    isCustom = false,
)

fun resolveCategory(categoryId: String): String {
    return DefaultCategory.entries.firstOrNull { it.name == categoryId }?.label ?: categoryId
}
