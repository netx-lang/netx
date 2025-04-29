#!/bin/sh

INSTALL_DIR="/usr/local/bin"
SCRIPT_FILE="$INSTALL_DIR/nx"
JAR_FILE="$INSTALL_DIR/nx.jar"

echo "Please ensure you have the necessary permissions to uninstall from $INSTALL_DIR."

echo "Uninstalling $JAR_FILE"
rm "$JAR_FILE"

echo "Uninstalling $SCRIPT_FILE"
rm "$SCRIPT_FILE"

xargs -I {} sh -c 'echo "-- Uninstalling: {}"; rm {}' < "build/nxsim/install_manifest.txt"

echo "Uninstallation completed."