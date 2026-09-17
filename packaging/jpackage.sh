#!/bin/sh
# Build a native installer/package with jpackage from an already-built dist.
#
#     ./gradlew installDist -Pversion=0.1.0
#     VERSION=0.1.0 ./packaging/jpackage.sh          # deb on Linux, dmg on macOS, msi on Windows
#     VERSION=0.1.0 ./packaging/jpackage.sh rpm
#
# Prerequisites:
#   Linux   : dpkg-deb (deb) / rpmbuild (rpm), fakeroot
#   macOS   : Xcode command line tools; a Developer ID identity to *distribute*
#   Windows : WiX Toolset 3.x on PATH (msi/exe)
#
# Note: these installers ship the `nx` CLI only. The C++ side (`nxsim_cli` plus
# the `nxsim` headers) comes with the archives built by packaging/assemble.py —
# use those if your users write C++ testbenches.
set -eu

VERSION=${VERSION:-0.1.0}
DIST=${DIST:-build/install/nx}
OUT=${OUT:-build/jpackage}
EXTRA_JAVA_OPTIONS=${EXTRA_JAVA_OPTIONS:-}

case "$(uname -s)" in
    Linux*)  DEFAULT_TYPE=deb ;;
    Darwin*) DEFAULT_TYPE=dmg ;;
    *)       DEFAULT_TYPE=msi ;;
esac
TYPE=${1:-$DEFAULT_TYPE}

MAIN_JAR="$DIST/lib/netx-$VERSION.jar"
if [ ! -f "$MAIN_JAR" ]; then
    echo "error: $MAIN_JAR not found; run './gradlew installDist -Pversion=$VERSION' first" >&2
    exit 1
fi

# jpackage wants a flat directory of files; feeding it $DIST/lib directly would
# also drag in the jlink runtime produced for the archives.
STAGE=$(mktemp -d)
trap 'rm -rf "$STAGE"' EXIT
cp "$DIST/lib/"*.jar "$STAGE/"

# shellcheck disable=SC2086
jpackage \
    --type "$TYPE" \
    --name netx \
    --app-version "$VERSION" \
    --vendor "PASCAL Lab, Nanjing University" \
    --description "NetX — a schematic programming language for hardware design" \
    --license-file LICENSE \
    --input "$STAGE" \
    --main-jar "netx-$VERSION.jar" \
    --main-class pascal.netx.Main \
    --dest "$OUT" \
    $EXTRA_JAVA_OPTIONS \
    --java-options "-Dfile.encoding=UTF-8"

echo "wrote $(ls -1 "$OUT" | tr '\n' ' ')to $OUT"
