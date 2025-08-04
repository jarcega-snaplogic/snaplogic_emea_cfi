# SnapLogic Pipeline Validation Hooks

Fast git pre-commit hooks that validate SnapLogic pipeline (.slp) files before commit to prevent invalid configurations from reaching the repository.

## Features

- **Fast validation** (<100ms for typical pipelines)
- **JSON syntax validation** catches malformed JSON
- **SnapLogic structure validation** ensures proper pipeline configuration
- **Silent success** for fast development workflow
- **Helpful error messages** with remediation steps

## Installation

```bash
./install-hooks.sh
```

The installer will:
1. Copy hooks to `.git/hooks/`
2. Make them executable
3. Test the installation

## What Gets Validated

### JSON Syntax
- Valid JSON structure
- Proper quote escaping
- No trailing commas

### SnapLogic Structure
- Pipeline class_id present
- Snap count matches link count (n snaps = n-1 links)
- UUID consistency between link_map and snap_map
- Required pipeline properties

## Usage

Hooks run automatically on `git commit`. If validation fails:

```bash
❌ JSON syntax error in: pipeline.slp  
   Run: python3 -m json.tool pipeline.slp
```

### Emergency Override

For urgent commits (use sparingly):
```bash
git commit --no-verify
```

## Files

- `pre-commit` - Main hook script
- `validate_structure.awk` - Fast structure validation
- `install-hooks.sh` - Hook installer
- `README.md` - This documentation

## Performance

- **Target**: <100ms validation time
- **Actual**: ~50ms for typical 3-snap pipeline
- **Method**: Single-pass AWK processing, early exit on errors
- **Scope**: Only validates changed .slp files

## Troubleshooting

### Hook Not Running
```bash
# Check if hooks are installed
ls -la .git/hooks/pre-commit

# Reinstall if missing
./hooks/install-hooks.sh
```

### False Positives
If the hook incorrectly rejects a valid pipeline, please report the issue with the specific .slp file that failed validation.

### Hook Updates
When hooks are updated in the repository, reinstall them:
```bash
./hooks/install-hooks.sh
```