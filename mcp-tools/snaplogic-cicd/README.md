# SnapLogic CI/CD MCP Tool

A comprehensive MCP (Model Context Protocol) tool for integrating SnapLogic pipelines with Git-based CI/CD workflows. This tool provides seamless synchronization between local pipeline development and SnapLogic platform deployment.

## Features

- **Push to SnapLogic**: Upload local pipeline files to SnapLogic platform
- **Pull from SnapLogic**: Download pipelines from SnapLogic to local repository
- **Sync Status**: Check synchronization status between local and remote
- **Pipeline Validation**: Comprehensive validation of pipeline files
- **Git Integration**: Automatic commit and push operations
- **Backup Management**: Automatic backup of existing pipelines before updates

## Installation

1. Place the tool in your project's `mcp-tools/snaplogic-cicd/` directory
2. Create configuration file: `.snaplogic-config.json`
3. Ensure Python dependencies are available: `requests`

## Configuration

Create a `.snaplogic-config.json` file in your project root:

```json
{
  "credentials": {
    "username": "your-email@company.com",
    "password": "your-password"
  },
  "api": {
    "base_url": "https://emea.snaplogic.com",
    "org": "YourOrganization",
    "project_space": "your-project-space",
    "project_id": "12345"
  },
  "sync": {
    "auto_commit": true,
    "commit_prefix": "[SnapLogic Sync]",
    "validate_before_push": true
  }
}
```

⚠️ **Security Note**: Add `.snaplogic-config.json` to your `.gitignore` file to avoid committing credentials.

## Usage

### Command Line Usage

```bash
# Push a pipeline to SnapLogic
claude-code mcp snaplogic-cicd push ProductSalesJoinPipeline.slp

# Pull all pipelines from SnapLogic
claude-code mcp snaplogic-cicd pull --all --commit

# Check sync status
claude-code mcp snaplogic-cicd status

# Validate a pipeline
claude-code mcp snaplogic-cicd validate ProductSalesJoinPipeline.slp

# List project assets
claude-code mcp snaplogic-cicd list
```

### Programmatic Usage

```python
from mcp_tools.snaplogic_cicd import push_to_snaplogic, pull_from_snaplogic, sync_status

# Push pipeline
result = push_to_snaplogic("ProductSalesJoinPipeline.slp", force=False, dry_run=False)
print(f"Status: {result['status']}, Message: {result['message']}")

# Pull pipeline
result = pull_from_snaplogic("MyPipeline", overwrite=True, commit=True)
print(f"Pulled files: {result.get('files', [])}")

# Check sync status
status = sync_status()
print(f"Local only: {status['local_only']}")
print(f"Remote only: {status['remote_only']}")
```

## Functions

### `push_to_snaplogic(pipeline_path, **options)`

Push a local pipeline file to SnapLogic platform.

**Parameters:**
- `pipeline_path` (str): Path to the .slp file
- `force` (bool): Force overwrite existing pipeline
- `dry_run` (bool): Simulate push without actual changes
- `backup` (bool): Create backup before overwriting
- `validate` (bool): Validate pipeline before pushing

**Returns:** `dict` with status and message

### `pull_from_snaplogic(pipeline_name=None, **options)`

Pull pipeline(s) from SnapLogic to local repository.

**Parameters:**
- `pipeline_name` (str, optional): Specific pipeline name, or None for all
- `overwrite` (bool): Overwrite existing local files
- `commit` (bool): Auto-commit pulled changes
- `commit_message` (str): Custom commit message

**Returns:** `dict` with status and list of pulled files

### `sync_status(**options)`

Check synchronization status between local and SnapLogic.

**Returns:** `dict` with status information:
- `local_only`: Pipelines only in local repository
- `remote_only`: Pipelines only in SnapLogic
- `both`: Pipelines in both locations
- `total_local`: Total local pipeline count
- `total_remote`: Total remote pipeline count

### `validate_pipeline(pipeline_path, **options)`

Validate a pipeline file before deployment.

**Parameters:**
- `pipeline_path` (str): Path to the .slp file

**Returns:** `dict` with validation results:
- `valid` (bool): Whether pipeline is valid
- `errors` (list): List of validation errors
- `warnings` (list): List of validation warnings
- `details` (dict): Detailed pipeline information

### `list_project_assets(**options)`

List all assets in the SnapLogic project.

**Parameters:**
- `asset_type` (str): Type of assets to list (default: "pipeline")

**Returns:** `list` of asset names

## Validation Rules

The tool validates pipelines against these criteria:

### Structure Validation
- Required fields: `class_id`, `class_version`, `property_map`, `snap_map`, `link_map`, `render_map`
- Proper pipeline class ID: `com-snaplogic-pipeline`
- Valid JSON syntax

### UUID Validation
- Proper UUID format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
- No duplicate UUIDs
- Sequential UUID pattern recommended: `11111111-1111-1111-1111-000000000xxx`

### Snap Validation
- Required snap fields: `class_id`, `instance_id`, `property_map`
- Instance ID matches snap ID
- Proper property map structure

### Link Validation
- Required link fields: `src_id`, `src_view_id`, `dst_id`, `dst_view_id`
- Source and destination snaps exist
- Valid view references

### Multi-Input Snap Validation
- Checks for missing `view_map` in multi-input snaps
- Warns about potential Designer display issues

## Error Handling

The tool provides comprehensive error handling:

- **Network Errors**: Retry logic for API calls
- **Authentication Errors**: Clear error messages for credential issues
- **Validation Errors**: Detailed validation reports with line numbers
- **File Errors**: Proper handling of missing or corrupted files

## Backup System

Automatic backups are created in `.snaplogic-backups/` directory:
- Timestamped backup files: `PipelineName_YYYYMMDD_HHMMSS.slp`
- Backups created before overwriting existing pipelines
- Configurable backup retention (manual cleanup required)

## Integration with Git Hooks

The tool can be integrated with Git hooks for automated synchronization:

### Post-Commit Hook Example

```bash
#!/bin/bash
# .git/hooks/post-commit

# Get list of changed .slp files
changed_pipelines=$(git diff --name-only HEAD~1 HEAD | grep '\\.slp$')

if [ -n "$changed_pipelines" ]; then
    echo "Pushing changed pipelines to SnapLogic..."
    for pipeline in $changed_pipelines; do
        claude-code mcp snaplogic-cicd push "$pipeline" --validate
    done
fi
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Check credentials in `.snaplogic-config.json`
   - Ensure account is not locked
   - Verify API base URL

2. **Pipeline Not Found**
   - Check pipeline name spelling
   - Verify project space and organization
   - Ensure pipeline exists in SnapLogic

3. **Validation Errors**
   - Run validation separately: `validate_pipeline()`
   - Check for JSON syntax errors
   - Verify UUID patterns and uniqueness

4. **Network Timeouts**
   - Check network connectivity
   - Verify SnapLogic platform availability
   - Consider increasing timeout values

### Debug Mode

Enable debug output by setting environment variable:
```bash
export SNAPLOGIC_DEBUG=1
```

## API Reference

The tool uses SnapLogic Public APIs:

- **Project Import**: `POST /api/1/rest/public/project/import/{org}/{space}/{project}`
- **Project Export**: `GET /api/1/rest/public/project/export/{org}/{space}/{project}`
- **Asset List**: `GET /api/1/rest/public/assets/{org}/{space}`
- **Git Pull**: `POST /api/1/rest/public/project/pull/{org}/{space}`

## Contributing

1. Follow Python coding standards
2. Add comprehensive error handling
3. Include unit tests for new functions
4. Update documentation for API changes
5. Test with actual SnapLogic environments

## Security Considerations

- Never commit credentials to version control
- Use environment variables for sensitive data in CI/CD
- Implement session token caching to minimize auth calls
- Regular credential rotation recommended
- Audit API access logs periodically

---

**Version**: 1.0.0  
**Author**: jarcega@snaplogic.com  
**License**: Internal Use Only