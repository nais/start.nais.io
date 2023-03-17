# start.nais.io

![workflow](https://github.com/nais/start.nais.io/actions/workflows/main.yaml/badge.svg)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/16219-nais-starter.svg)](https://plugins.jetbrains.com/)
![vscode marketplace](https://vsmarketplacebadge.apphb.com/version/navikt.nais-starter-vscode.svg)

Lets [NAIS](https://nais.io) users generate basic YAML incantations for building and deploying their apps with minimal effort, thus eliminating the need for a lot of manual text editing.

Inspired by the likes of [start.ktor.io](https://start.ktor.io) and [start.spring.io](https://start.spring.io).

## ⌨️ Usage

This service kan be used in to ways:
- Through the [web interface](https://start.nais.io).
- Through the extensions for [IntelliJ IDEA](https://plugins.jetbrains.com/plugin/16219-nais-starter) or [Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=navikt.nais-starter-vscode).


## ⚖️ License
[MIT](LICENSE).

## 👥 Contact

This project is maintained by [@nais](https://github.com/nais).

Questions and/or feature requests? Please create an [issue](https://github.com/nais/start.nais.io/issues).

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack channel [#nais](https://nav-it.slack.com/archives/C5KUST8N6).

## Verifying the start.nais.io image and its contents

The image is signed "keylessly" using [Sigstore cosign](https://github.com/sigstore/cosign).
To verify its authenticity run
```
cosign verify \
--certificate-identity "https://github.com/nais/start.nais.io/.github/workflows/main.yaml@refs/heads/main" \
--certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
ghcr.io/nais/start.nais.io@sha256:<shasum>
```

The images are also attested with SBOMs in the [CycloneDX](https://cyclonedx.org/) format.
You can verify these by running
```
cosign verify-attestation --type cyclonedx \
--certificate-identity "https://github.com/nais/start.nais.io/.github/workflows/main.yaml@refs/heads/main" \
--certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
ghcr.io/nais/start.nais.io@sha256:<shasum>
```


