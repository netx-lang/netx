#!/bin/sh

# NetX Cross-Platform Installation Script
# Supports Windows (Git Bash/MSYS2), macOS, and Linux

set -e

# Detect platform
detect_platform() {
    case "$(uname -s)" in
        Linux*)     echo "linux";;
        Darwin*)    echo "macos";;
        CYGWIN*|MINGW*|MSYS*) echo "windows";;
        *)          echo "unknown";;
    esac
}

# Get default install directory based on platform
get_default_install_dir() {
    platform=$(detect_platform)
    case "$platform" in
        windows)
            echo "${USERPROFILE}/.local/netx"
            ;;
        *)
            echo "${HOME}/.local/netx"
            ;;
    esac
}

# Configuration
PLATFORM=$(detect_platform)
DEFAULT_INSTALL_DIR=$(get_default_install_dir)
INSTALL_DIR="${INSTALL_PREFIX:-${INSTALL_DIR:-$DEFAULT_INSTALL_DIR}}"
DIST_DIR="build/install/nx"
BIN_DIR="$INSTALL_DIR/bin"
LIB_DIR="$INSTALL_DIR/lib"

echo "NetX Installation Script"
echo "Platform: $PLATFORM"
echo "Install directory: $INSTALL_DIR"
echo

# Check for required tools
check_requirements() {
    echo "Checking requirements..."

    if ! command -v java >/dev/null 2>&1; then
        echo "Error: Java is required but not installed."
        echo "Please install Java 17 or later and try again."
        exit 1
    fi

    if ! command -v cmake >/dev/null 2>&1; then
        echo "Error: CMake is required but not installed."
        echo "Please install CMake and try again."
        exit 1
    fi

    echo "Requirements check passed."
}

# Create directories (cross-platform)
create_directories() {
    echo "Creating directories..."
    mkdir -p "$BIN_DIR" "$LIB_DIR"
}

# Build the distribution
echo "Building NetX distribution..."
if [ "$PLATFORM" = "windows" ]; then
    ./gradlew.bat installDist buildCMake
else
    ./gradlew installDist buildCMake
fi

check_requirements
create_directories

# Copy all JAR files to lib directory (separate, not bundled)
echo "Installing libraries..."
cp -r "$DIST_DIR/lib/"* "$LIB_DIR/"

# Install C++ simulator components
echo "Installing C++ simulator..."
if [ -d "build/nxsim" ]; then
    # Install the built C++ binaries
    cmake --install build/nxsim --prefix "$INSTALL_DIR" 2>/dev/null || {
        echo "Warning: CMake install failed, copying binaries manually..."
        # Fallback: copy binaries manually
        if [ -d "build/nxsim/src" ]; then
            mkdir -p "$BIN_DIR"
            find build/nxsim/src -name "*.exe" -o -name "nxsim" -o -name "simulator" 2>/dev/null | while read -r binary; do
                if [ -f "$binary" ] && [ -x "$binary" ]; then
                    cp "$binary" "$BIN_DIR/"
                    echo "Copied: $(basename "$binary")"
                fi
            done
        fi
    }

    # Also copy simulator source/config files if needed
    if [ -d "simulator" ]; then
        echo "Installing simulator configuration..."
        mkdir -p "$INSTALL_DIR/share/netx"
        cp -r simulator/* "$INSTALL_DIR/share/netx/" 2>/dev/null || true
    fi
else
    echo "Warning: No C++ build found at build/nxsim"
    echo "Run './gradlew buildCMake' first if you need the C++ simulator"
fi

# Ensure C++ static/shared libraries are installed
NXSIM_LIB_SRC="build/nxsim/src"
if [ -d "$NXSIM_LIB_SRC" ]; then
    for libfile in libnxsim.a libnxsim.so; do
        if [ -f "$NXSIM_LIB_SRC/$libfile" ]; then
            cp "$NXSIM_LIB_SRC/$libfile" "$LIB_DIR/"
            echo "Installed $libfile to $LIB_DIR/"
        fi
    done
else
    echo "Warning: $NXSIM_LIB_SRC does not exist, cannot install C++ libraries."
fi

# Copy and configure the startup script
echo "Installing startup script..."
if [ "$PLATFORM" = "windows" ]; then
    # Copy both Unix script and Windows batch file
    cp "$DIST_DIR/bin/nx" "$BIN_DIR/"
    cp "$DIST_DIR/bin/nx.bat" "$BIN_DIR/"
    chmod +x "$BIN_DIR/nx" 2>/dev/null || true

    # Update both scripts to use the correct lib path
    sed -i "s|APP_HOME=.*|APP_HOME=\"$INSTALL_DIR\"|" "$BIN_DIR/nx" 2>/dev/null || true
    # For Windows batch file, update the path differently
    sed -i "s|set APP_HOME=.*|set APP_HOME=$INSTALL_DIR|" "$BIN_DIR/nx.bat" 2>/dev/null || true
else
    cp "$DIST_DIR/bin/nx" "$BIN_DIR/"
    chmod +x "$BIN_DIR/nx"

    # Update the script to use the correct lib path
    sed -i "s|APP_HOME=.*|APP_HOME=\"$INSTALL_DIR\"|" "$BIN_DIR/nx"
fi

# Copy C++ headers to include directory
SIM_SRC_DIR="simulator/src"
INCLUDE_DIR="$INSTALL_DIR/include"
echo "Installing C++ headers..."
if [ -d "$SIM_SRC_DIR" ]; then
    mkdir -p "$INCLUDE_DIR"
    # Copy all .h and .hpp files, preserving directory structure
    find "$SIM_SRC_DIR" \( -name '*.h' -o -name '*.hpp' \) | while read -r header; do
        relpath="${header#$SIM_SRC_DIR/}"
        targetdir="$INCLUDE_DIR/$(dirname "$relpath")"
        mkdir -p "$targetdir"
        cp "$header" "$targetdir/"
    done
else
    echo "Warning: No C++ headers found in $SIM_SRC_DIR"
fi

# Function to detect user's shell and get config file
detect_shell_config() {
    # Get the user's shell
    user_shell=$(basename "$SHELL" 2>/dev/null || echo "bash")

    case "$user_shell" in
        zsh)
            if [ -f "$HOME/.zshrc" ]; then
                echo "$HOME/.zshrc"
            else
                echo "$HOME/.zshrc"
            fi
            ;;
        fish)
            mkdir -p "$HOME/.config/fish"
            echo "$HOME/.config/fish/config.fish"
            ;;
        bash)
            if [ -f "$HOME/.bashrc" ]; then
                echo "$HOME/.bashrc"
            elif [ -f "$HOME/.bash_profile" ]; then
                echo "$HOME/.bash_profile"
            else
                echo "$HOME/.bashrc"
            fi
            ;;
        *)
            # Default to .bashrc for unknown shells
            echo "$HOME/.bashrc"
            ;;
    esac
}

# Function to add environment variables to shell config
setup_environment() {
    if [ "$PLATFORM" = "windows" ]; then
        echo "Note: On Windows, you may need to manually add $BIN_DIR to your PATH environment variable."
        return
    fi

    config_file=$(detect_shell_config)
    shell_name=$(basename "$SHELL" 2>/dev/null || echo "bash")

    echo "Setting up environment variables in $config_file..."

    # Create config file if it doesn't exist
    touch "$config_file"

    # Check if NetX environment is already configured
    if grep -q "# NetX Environment" "$config_file" 2>/dev/null; then
        echo "NetX environment variables already configured in $config_file"
        echo "If you want to reconfigure, please remove the NetX section manually and run install again."
        return
    fi

    # Add NetX environment configuration
    if [ "$shell_name" = "fish" ]; then
        # Fish shell syntax
        cat >> "$config_file" << EOF

# NetX Environment - START
set -gx PATH "$BIN_DIR" \$PATH
set -gx CPLUS_INCLUDE_PATH "$INCLUDE_DIR" \$CPLUS_INCLUDE_PATH
set -gx C_INCLUDE_PATH "$INCLUDE_DIR" \$C_INCLUDE_PATH
set -gx LIBRARY_PATH "$LIB_DIR" \$LIBRARY_PATH
set -gx LD_LIBRARY_PATH "$LIB_DIR" \$LD_LIBRARY_PATH
# NetX Environment - END
EOF
    else
        # Bash/Zsh syntax
        cat >> "$config_file" << EOF

# NetX Environment - START
export PATH="$BIN_DIR:\$PATH"
export CPLUS_INCLUDE_PATH="$INCLUDE_DIR:\$CPLUS_INCLUDE_PATH"
export C_INCLUDE_PATH="$INCLUDE_DIR:\$C_INCLUDE_PATH"
export LIBRARY_PATH="$LIB_DIR:\$LIBRARY_PATH"
export LD_LIBRARY_PATH="$LIB_DIR:\$LD_LIBRARY_PATH"
# NetX Environment - END
EOF
    fi

    echo "Environment variables added to $config_file"
    echo "Please restart your shell or run: source $config_file"
}

# Add to PATH suggestion
echo
echo "NetX installed successfully!"
echo "Installation directory: $INSTALL_DIR"
echo

# Automatically setup environment variables
setup_environment

if [ "$PLATFORM" = "windows" ]; then
    echo "To use NetX, either:"
    echo "  1. Run: $BIN_DIR/nx.bat [arguments]"
    echo "  2. Add $BIN_DIR to your PATH environment variable"
else
    echo "Environment variables have been automatically configured for your shell."
    echo "To use NetX immediately, either:"
    echo "  1. Restart your terminal/shell"
    echo "  2. Run: source $(detect_shell_config)"
fi

echo
echo "For system-wide installation, run with:"
echo "  INSTALL_PREFIX=/usr/local ./install.sh"
echo
echo "To uninstall, run: ./uninstall.sh"

# Updated C++ development instructions
if [ "$PLATFORM" != "windows" ]; then
    echo "For C++ development:"
    echo "  - Headers and libraries are automatically configured via environment variables"
    echo "  - You can now simply use: g++ myfile.cpp -flto -lnxsim"
    echo
fi
