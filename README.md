# coppercore


## Maven Central
This project is available on maven central! The instructions below outline how to set this up.

### Secret Keys

In order to publish on Maven Central, multiple secret keys must be maintained. The GPG key and the Sonatype token are independent though, and can be regenerated as needed without updating the other one.

**Note:** When creating a new GPG key, you must also publish the public key. See the section below on generating the GPG key for details.

The following secret keys are required to be configured in this repository's Github secrets in order to publish it to maven central:
- `ORG_GRADLE_PROJECT_mavenCentralUsername`: the username **generated from the Sonatype Central Portal** for the current user token, not to be confused with the username of the account.
- `ORG_GRADLE_PROJECT_mavenCentralPassword`: the password generated when the user token is generated. This is linked with the username above and can be generated from the Sonatype Central Portal.
- `ORG_GRADLE_PROJECT_signingInMemoryKey`: The private GPG signing key used for signing the package. Generating this is explained below.
- `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`: The password generated during the GPG key generation, if one is chosen.

It also works to include these as organization secrets in GitHub.

To temporarily set any of these variables locally for testing, you can do the following in VSCode powershell terminals:
```
$env.ENV_VARIABLE_NAME = "environment-variable-value"
```

More information about environment variables can be found [on the docs page of our gradle plugin of choice](https://vanniktech.github.io/gradle-maven-publish-plugin/central/#secrets) for publishing to the Central Portal. 

### Generating a GPG key

To generate the GPG key, first generate a key locally:
```
gpg --full-generate-key
```

Pick RSA (default option) and then use a key with 4096 bits. The rest of the process is straightforward, just use the team email account (401frc@gmail.com) and your name for the user.

List the keys using this command and find the one you just generated:
```
gpg --list-secret-keys
```

Then to get the private key in plaintext and save it to a file called `gpg.key` (for easier copy and memory of what the key is), run the following:
```
gpg --export-secret-keys --armor KEY-ID | grep -v '\-\-' | grep -v '^=.' | tr -d '\n' > gpg.key
```
where `KEY-ID` is the long string of numbers and letters in the `sec` section of the key that you just generated.

The long string of the plaintext key is what needs to be set for the `ORG_GRADLE_PROJECT_signingInMemoryKey` environment variable. **This is the private key, so DO NOT share it with anyone or check it into the repository!!!**

**Note:** if you configure a passphrase for your key, you'll need to add that to a `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` environment variable.

Finally, you need to publish the **public** key so users can actually verify the signature. Do that with the following command:
```
gpg --keyserver keyserver.ubuntu.com KEY-ID
```

More information on generating GPG keys can be found [on the Sonatype docs](https://central.sonatype.org/publish/requirements/gpg/) or on the [GitHub docs](https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key).

### Publishing

#### GitHub Actions

Whenever a release is created on GitHub, the `publish_library` action will automatically publish the released version to the Central Portal. At any time this workflow can be run manually as well.

The `stage_library` GitHub action can be run manually to confirm that the library stages on the Central Portal. This can be used to preview a release to the Central Portal and also to ensure the maven central formatting is followed correctly.

#### Locally
To stage the package on the Central Portal for future (manual) release, run the following command:
```
./gradlew publishToMavenCentral --no-configuration-cache
```

Or, to automatically stage and publish a version run:
```
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```