val lspatch =
    tasks.register<VerifiedDownload>("downloadLspatch") {
        url = "https://github.com/JingMatrix/LSPatch/releases/download/v1.2/lspatch-v1.2-487-release.jar"
        sha256 = "d238fdc414d121b7fa454d8b4ccf420df3a8c97d563761861ff92bd9c5da2165"
        output = layout.buildDirectory.file("lspatch/lspatch.jar")
    }

dependencies { implementation(files(lspatch)) }
