package com.tyshi00.ledger.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Calendar

class LedgerRepository(private val db: LedgerDatabase) {

    private val entries get() = db.entryDao()
    private val customCats get() = db.customCategoryDao()
    private val budgets get() = db.budgetTargetDao()
    private val prefs get() = db.prefDao()

    // ── Entries ──

    suspend fun getEntriesByMonth(year: Int, month: Int) = entries.getByMonth(year, month)

    suspend fun getEntriesByMonthAndType(year: Int, month: Int, type: EntryType) =
        entries.getByMonthAndType(year, month, type.name)

    suspend fun sumByType(year: Int, month: Int, type: EntryType) =
        entries.sumByType(year, month, type.name)

    suspend fun getEntry(id: Long) = entries.getById(id)

    suspend fun addEntry(
        amountCents: Long,
        categoryId: String,
        type: EntryType,
        frequency: Frequency,
        note: String?,
        year: Int,
        month: Int,
        day: Int,
    ) {
        entries.insert(
            LedgerEntry(
                amountCents = amountCents,
                categoryId = categoryId,
                type = type.name,
                frequency = frequency.name,
                note = note,
                year = year,
                month = month,
                day = day,
            )
        )
    }

    suspend fun deleteEntry(id: Long) = entries.deleteById(id)

    // ── Custom Categories ──

    suspend fun getCustomCategoriesByGroup(group: CategoryGroup) =
        customCats.getByGroup(group.name)

    suspend fun addCustomCategory(label: String, group: CategoryGroup) {
        val existing = customCats.getByGroup(group.name)
        customCats.insert(
            CustomCategory(
                id = "custom_${System.currentTimeMillis()}",
                label = label,
                parentGroup = group.name,
                type = group.type.name,
                sortOrder = existing.size,
            )
        )
    }

    suspend fun deleteCustomCategory(id: String) = customCats.deleteById(id)

    suspend fun renameCustomCategory(id: String, newName: String) {
        val existing = db.customCategoryDao().getAll().firstOrNull { it.id == id } ?: return
        db.customCategoryDao().insert(existing.copy(label = newName))
    }

    suspend fun deleteCategoryAndEntries(id: String) {
        customCats.deleteById(id)
        deleteEntriesByCategory(id)
    }

    suspend fun deleteEntriesByCategory(categoryId: String) {
        val all = entries.getAll().filter { it.categoryId == categoryId }
        all.forEach { entries.deleteById(it.id) }
    }

    suspend fun getCategoriesForGroup(group: CategoryGroup): List<CategoryItem> {
        val hidden = getHiddenCategories()
        val defaults = DefaultCategory.entries
            .filter { it.group == group && it.name !in hidden }
            .map { it.toItem() }
        val custom = customCats.getByGroup(group.name)
            .filter { it.id !in hidden }
            .map { CategoryItem(it.id, it.label, group, isCustom = true) }
        return defaults + custom
    }

    // ── Budget Targets (with carryover) ──

    /** Gets budget for a group+month, falling back to the most recent prior month if unset. */
    suspend fun getBudgetForGroup(group: CategoryGroup, year: Int, month: Int): Long {
        // Check this month first
        val thisMonth = budgets.getByGroupAndMonth(group.name, year, month)
        if (thisMonth != null) return thisMonth.amountCents

        // Carryover: find the most recent budget for this group
        val all = budgets.getAll()
            .filter { it.groupName == group.name }
            .sortedWith(compareByDescending<BudgetTarget> { it.year }.thenByDescending { it.month })

        val previous = all.firstOrNull {
            it.year < year || (it.year == year && it.month < month)
        }
        return previous?.amountCents ?: 0L
    }

    suspend fun setBudgetForGroup(group: CategoryGroup, year: Int, month: Int, amountCents: Long) {
        budgets.upsert(
            BudgetTarget(
                id = "${group.name}_${year}_${month}",
                groupName = group.name,
                type = group.type.name,
                amountCents = amountCents,
                year = year,
                month = month,
            )
        )
    }

    suspend fun totalBudgetByType(type: EntryType, year: Int, month: Int): Long {
        return CategoryGroup.entries
            .filter { it.type == type }
            .sumOf { getBudgetForGroup(it, year, month) }
    }

    // ── Preferences ──

    private suspend fun getPref(key: String): String? = prefs.get(key)?.value
    private suspend fun setPref(key: String, value: String) = prefs.set(PrefEntry(key, value))

    suspend fun getHiddenCategories(): Set<String> {
        val raw = getPref("hidden_categories") ?: return emptySet()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    suspend fun setHiddenCategories(hidden: Set<String>) {
        setPref("hidden_categories", hidden.joinToString(","))
    }

    suspend fun getCurrencySymbol(): String = getPref("currency_symbol") ?: "$"
    suspend fun setCurrencySymbol(symbol: String) = setPref("currency_symbol", symbol)

    suspend fun getInvertColors(): Boolean = getPref("invert_colors") == "true"
    suspend fun setInvertColors(value: Boolean) = setPref("invert_colors", value.toString())

    // ── PIN Security ──

    suspend fun isPinEnabled(): Boolean {
        android.util.Log.d("LedgerPIN", "isPinEnabled check: pin_hash=${getPref("pin_hash")}")
        val hash = getPref("pin_hash")
        return hash != null && hash.isNotEmpty()
    }

    suspend fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        setPref("pin_hash", hash.toHex())
        setPref("pin_salt", salt.toHex())
    }

    suspend fun verifyPin(pin: String): Boolean {
        val storedHash = getPref("pin_hash")?.takeIf { it.isNotEmpty() }?.fromHex() ?: return false
        val salt = getPref("pin_salt")?.takeIf { it.isNotEmpty() }?.fromHex() ?: return false
        return MessageDigest.isEqual(hashPin(pin, salt), storedHash)
    }

    suspend fun clearPin() {
        setPref("pin_hash", "")
        setPref("pin_salt", "")
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").apply { update(salt) }.digest(pin.toByteArray())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.fromHex(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in result.indices) {
            result[i] = ((Character.digit(this[i * 2], 16) shl 4) + Character.digit(this[i * 2 + 1], 16)).toByte()
        }
        return result
    }

    // ── Export ──

    suspend fun exportAll(): ExportData {
        return ExportData(
            entries = db.entryDao().getAll(),
            customCategories = db.customCategoryDao().getAll(),
            budgetTargets = db.budgetTargetDao().getAll(),
            preferences = db.prefDao().getAll(),
        )
    }

    suspend fun resetAll() {
        db.entryDao().deleteAll()
        db.customCategoryDao().deleteAll()
        db.budgetTargetDao().deleteAll()
        db.prefDao().deleteAll()
    }

    /** Resolve a category ID to its display label (checks defaults then custom DB). */
    suspend fun resolveCategoryLabel(categoryId: String): String {
        val default = DefaultCategory.entries.firstOrNull { it.name == categoryId }
        if (default != null) return default.label
        val custom = db.customCategoryDao().getAll().firstOrNull { it.id == categoryId }
        if (custom != null) return custom.label
        return categoryId
    }

    companion object {
        @Volatile private var instance: LedgerRepository? = null
        fun getInstance(factory: () -> LedgerDatabase): LedgerRepository =
            instance ?: synchronized(this) {
                instance ?: LedgerRepository(factory()).also { instance = it }
            }
    }
}

data class ExportData(
    val entries: List<LedgerEntry>,
    val customCategories: List<CustomCategory>,
    val budgetTargets: List<BudgetTarget>,
    val preferences: List<PrefEntry>,
)
