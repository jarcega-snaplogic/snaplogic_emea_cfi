#!/bin/bash
# SnapLogic Pipeline Validation Hook Installer
# Installs pre-commit hook for .slp file validation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GIT_HOOKS_DIR="$(git rev-parse --git-dir)/hooks"

echo "Installing SnapLogic validation hooks..."

# Check if we're in a git repository
if ! git rev-parse --git-dir >/dev/null 2>&1; then
    echo "Error: Not in a git repository"
    exit 1
fi

# Copy hook files
cp "$SCRIPT_DIR/pre-commit" "$GIT_HOOKS_DIR/"
cp "$SCRIPT_DIR/validate_structure.awk" "$GIT_HOOKS_DIR/"

# Make executable
chmod +x "$GIT_HOOKS_DIR/pre-commit"
chmod +x "$GIT_HOOKS_DIR/validate_structure.awk"

echo "✅ Hooks installed successfully!"
echo ""
echo "The pre-commit hook will now validate .slp files automatically."
echo "To bypass validation (emergency only): git commit --no-verify"
echo ""

# Test installation
if [ -f *.slp ] 2>/dev/null; then
    echo "Testing hook installation..."
    if "$GIT_HOOKS_DIR/pre-commit"; then
        echo "✅ Hook test passed!"
    else
        echo "❌ Hook test failed - check existing .slp files"
        exit 1
    fi
else
    echo "No .slp files found to test, but installation completed successfully."
fi