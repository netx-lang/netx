#!/bin/sh

JAR_FILE="build/libs/netx-1.0-SNAPSHOT.jar"
SIM_BUILD_DIR="build/nxsim"
INSTALL_DIR="/usr/local/bin"
SCRIPT_FILE="$INSTALL_DIR/nx"

echo "Please ensure you have the necessary permissions to install file into $INSTALL_DIR."

echo "Installing $JAR_FILE to $INSTALL_DIR"
cp "$JAR_FILE" "$INSTALL_DIR/nx.jar"

echo "Installing script to $SCRIPT_FILE"
echo "#!/bin/sh" > "$SCRIPT_FILE"
echo "java -jar $INSTALL_DIR/nx.jar \"\$@\"" >> "$SCRIPT_FILE"
chmod +x "$SCRIPT_FILE"

cmake --install $SIM_BUILD_DIR