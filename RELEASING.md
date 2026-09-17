# Releasing NetX

Pushing a version tag runs the [release workflow](.github/workflows/release.yml),
which builds, smoke-tests, and publishes archives to GitHub Releases.

## Release steps

1. Run `./gradlew test` and the C++ test suite.
2. Commit the release changes and tag that commit. Replace `v0.1.0` below with
   the release version:

   ```bash
   git tag -a v0.1.0 -m "NetX 0.1.0"
   git push origin main
   git push origin v0.1.0
   ```

   The release version comes from the tag. The default for local builds is set
   in [shared-build-conventions.gradle.kts](buildSrc/src/main/kotlin/shared-build-conventions.gradle.kts).
3. Confirm the workflow succeeds and the release contains all five platform
   archives plus `SHA256SUMS`.
4. Review the release notes and update the website's download links. Update any
   maintained package-manager manifests with the new URLs and checksums.

## Release contents

Archives are provided for Linux and macOS (x64 and arm64), and Windows (x64).
Each includes `nx`, a bundled Java runtime, `nxsim_cli`, and the `nxsim` headers
and CMake package. Linux binaries require glibc 2.39 or newer, such as Ubuntu 24.04+.

For build settings and local packaging commands, see the
[release workflow](.github/workflows/release.yml) and
[archive assembler](packaging/assemble.py).
[Native installers](packaging/jpackage.sh) are optional and include only `nx`.
