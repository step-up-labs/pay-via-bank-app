package io.stepuplabs.pvba

/**
 * This is a default list of tested and supported bank app package names.
 * It's recommended to put this list into a remote configuration so you can update it without app update.
 */
val DEFAULT_BANK_APP_PACKAGES = listOf(
    "cz.rb.app.smartphonebanking", // Raiffeisenbank
    "eu.inmite.prj.kb.mobilbank", // Komerční banka (old app)
    "cz.kb.ndb", // Komerční banka (new app)
    "cz.csas.georgego", // Česká spořitelna (George)
    "cz.csob.smart", // ČSOB
    "cz.moneta.smartbanka", // Moneta
    "cz.fio.sb2", // Fio banka
    "cz.airbank.android", // Air Bank
    "cz.creditas.richee", // Banka CREDITAS (old app)
    "com.onebank", // Banka CREDITAS (new app)
    "cz.pbktechnology.partners.client", // Partners Banka
    "hr.asseco.android.jimba.mUCI.cz", // UniCredit Bank
    "cz.mbank", // mBank
    "com.jtfg.jtbank.prod" // J&T Banka
)