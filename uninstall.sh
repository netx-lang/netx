#!/bin/sh

# NetX Cross-Platform Uninstallation Script
# Supports Windows (Git Bash/MSYS2), macOS, and Linux

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

echo "NetX Uninstallation Script"
echo "Platform: $PLATFORM"
echo "Looking for installation in: $INSTALL_DIR"
echo

# Check if installation exists
if [ ! -d "$INSTALL_DIR" ]; then
    echo "No NetX installation found at $INSTALL_DIR"
    echo "If you installed to a different location, run:"
    echo "  INSTALL_PREFIX=/your/custom/path ./uninstall.sh"
    exit 1
fi

# Confirm uninstallation
echo "This will remove NetX from: $INSTALL_DIR"
printf "Are you sure? (y/N): "
read -r response

case "$response" in
    [yY]|[yY][eE][sS])
        echo "Proceeding with uninstallation..."
        ;;
    *)
        echo "Uninstallation cancelled."
        exit 0
        ;;
esac

# Function to detect user's shell and get config file (same as install.sh)
detect_shell_config() {
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
            echo "$HOME/.bashrc"
            ;;
    esac
}

# Function to remove NetX environment variables from shell config
cleanup_environment() {
    if [ "$PLATFORM" = "windows" ]; then
        echo "Note: On Windows, you may need to manually remove NetX from your PATH environment variable."
        return
    fi

    config_file=$(detect_shell_config)

    if [ ! -f "$config_file" ]; then
        echo "No shell config file found at $config_file"
        return
    fi

    # Check if NetX environment section exists
    if ! grep -q "# NetX Environment - START" "$config_file" 2>/dev/null; then
        echo "No NetX environment configuration found in $config_file"
        return
    fi

    echo "Removing NetX environment variables from $config_file..."

    # Create a temporary file to store the cleaned config
    temp_file=$(mktemp)

    # Remove the NetX Environment section (from START to END markers)
    awk '
    /^# NetX Environment - START$/ { in_netx = 1; next }
    /^# NetX Environment - END$/ { in_netx = 0; next }
    !in_netx { print }
    ' "$config_file" > "$temp_file"

    # Replace the original file with the cleaned version
    mv "$temp_file" "$config_file"

    echo "NetX environment variables removed from $config_file"
    echo "Please restart your shell or run: source $config_file"
}

# Remove the installation directory
echo "Removing NetX installation..."
rm -rf "$INSTALL_DIR"

if [ $? -eq 0 ]; then
    echo "NetX installation removed successfully from $INSTALL_DIR"
else
    echo "Error: Failed to remove NetX installation directory"
    exit 1
fi

# Clean up environment variables
cleanup_environment

echo
echo "NetX has been completely uninstalled."
echo "You may want to restart your shell to ensure environment variables are updated."
