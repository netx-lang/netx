#!/bin/sh

JAR_FILE="build/libs/netx-1.0-SNAPSHOT.jar"
INSTALL_DIR="/usr/local/bin"
SCRIPT_FILE="$INSTALL_DIR/nx"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: $JAR_FILE not found, please run 'gradle build' first."
    exit 1
fi

cp "$JAR_FILE" "$INSTALL_DIR/nx.jar"

echo "#!/bin/sh" > "$SCRIPT_FILE"
echo "java -jar $INSTALL_DIR/nx.jar \"\$@\"" >> "$SCRIPT_FILE"

chmod +x "$SCRIPT_FILE"