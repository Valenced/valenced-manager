package app.valenced.manager.di

import app.valenced.manager.ui.viewmodel.home.HomeViewModel
import app.valenced.manager.ui.viewmodel.installer.InstallerViewModel
import app.valenced.manager.ui.viewmodel.installer.LogViewerViewModel
import app.valenced.manager.ui.viewmodel.libraries.LibrariesViewModel
import app.valenced.manager.ui.viewmodel.settings.AdvancedSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::AdvancedSettingsViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::LogViewerViewModel)
    factoryOf(::LibrariesViewModel)
}