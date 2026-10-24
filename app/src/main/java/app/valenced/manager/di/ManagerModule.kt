package app.valenced.manager.di

import app.valenced.manager.domain.manager.DownloadManager
import app.valenced.manager.domain.manager.InstallManager
import app.valenced.manager.domain.manager.PreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::DownloadManager)
    singleOf(::PreferenceManager)
    singleOf(::InstallManager)
}